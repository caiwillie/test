package io.camunda.operate.webapp.es.writer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.entities.BatchOperationEntity;
import io.camunda.operate.entities.IncidentEntity;
import io.camunda.operate.entities.OperationEntity;
import io.camunda.operate.entities.OperationState;
import io.camunda.operate.entities.OperationType;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.exceptions.PersistenceException;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.schema.templates.BatchOperationTemplate;
import io.camunda.operate.schema.templates.ListViewTemplate;
import io.camunda.operate.schema.templates.OperationTemplate;
import io.camunda.operate.util.CollectionUtil;
import io.camunda.operate.util.ConversionUtils;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.util.ElasticsearchUtil.QueryType;
import io.camunda.operate.webapp.es.reader.IncidentReader;
import io.camunda.operate.webapp.es.reader.ListViewReader;
import io.camunda.operate.webapp.es.reader.OperationReader;
import io.camunda.operate.webapp.rest.dto.operation.CreateBatchOperationRequestDto;
import io.camunda.operate.webapp.rest.dto.operation.CreateOperationRequestDto;
import io.camunda.operate.webapp.rest.exception.InvalidRequestException;
import io.camunda.operate.webapp.rest.exception.NotFoundException;
import io.camunda.operate.webapp.security.UserService;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.ConstantScoreQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BatchOperationWriter {
   private static final Logger logger = LoggerFactory.getLogger(BatchOperationWriter.class);
   @Autowired
   private ListViewReader listViewReader;
   @Autowired
   private IncidentReader incidentReader;
   @Autowired
   private OperateProperties operateProperties;
   @Autowired
   private RestHighLevelClient esClient;
   @Autowired
   private ObjectMapper objectMapper;
   @Autowired
   private OperationTemplate operationTemplate;
   @Autowired
   private OperationReader operationReader;
   @Autowired
   private ListViewTemplate listViewTemplate;
   @Autowired
   private BatchOperationTemplate batchOperationTemplate;
   @Autowired
   private UserService userService;

   public List lockBatch() throws PersistenceException {
      String workerId = this.operateProperties.getOperationExecutor().getWorkerId();
      long lockTimeout = this.operateProperties.getOperationExecutor().getLockTimeout();
      int batchSize = this.operateProperties.getOperationExecutor().getBatchSize();
      List operationEntities = this.operationReader.acquireOperations(batchSize);
      BulkRequest bulkRequest = new BulkRequest();
      Iterator var7 = operationEntities.iterator();

      while(var7.hasNext()) {
         OperationEntity operation = (OperationEntity)var7.next();
         operation.setState(OperationState.LOCKED);
         operation.setLockOwner(workerId);
         operation.setLockExpirationTime(OffsetDateTime.now().plus(lockTimeout, ChronoUnit.MILLIS));
         bulkRequest.add(this.createUpdateByIdRequest(operation, false));
      }

      ElasticsearchUtil.processBulkRequest(this.esClient, bulkRequest, true);
      logger.debug("{} operations locked", operationEntities.size());
      return operationEntities;
   }

   private UpdateRequest createUpdateByIdRequest(OperationEntity operation, boolean refreshImmediately) throws PersistenceException {
      try {
         Map jsonMap = (Map)this.objectMapper.readValue(this.objectMapper.writeValueAsString(operation), HashMap.class);
         UpdateRequest updateRequest = ((UpdateRequest)(new UpdateRequest()).index(this.operationTemplate.getFullQualifiedName())).id(operation.getId()).doc(jsonMap);
         if (refreshImmediately) {
            updateRequest = updateRequest.setRefreshPolicy(RefreshPolicy.IMMEDIATE);
         }

         return updateRequest;
      } catch (IOException var5) {
         throw new PersistenceException(String.format("Error preparing the query to update operation [%s] for process instance id [%s]", operation.getId(), operation.getProcessInstanceKey()), var5);
      }
   }

   public void updateOperation(OperationEntity operation) throws PersistenceException {
      UpdateRequest updateRequest = this.createUpdateByIdRequest(operation, true);
      ElasticsearchUtil.executeUpdate(this.esClient, updateRequest);
   }

   public BatchOperationEntity scheduleBatchOperation(CreateBatchOperationRequestDto batchOperationRequest) {
      logger.debug("Creating batch operation: operationRequest [{}]", batchOperationRequest);

      try {
         BatchOperationEntity batchOperation = this.createBatchOperationEntity(batchOperationRequest.getOperationType(), batchOperationRequest.getName());
         int batchSize = this.operateProperties.getElasticsearch().getBatchSize();
         ConstantScoreQueryBuilder query = this.listViewReader.createProcessInstancesQuery(batchOperationRequest.getQuery());
         ElasticsearchUtil.QueryType queryType = QueryType.ONLY_RUNTIME;
         if (batchOperationRequest.getOperationType().equals(OperationType.DELETE_PROCESS_INSTANCE)) {
            queryType = QueryType.ALL;
         }

         SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest(this.listViewTemplate, queryType).source((new SearchSourceBuilder()).query(query).size(batchSize).fetchSource(false));
         AtomicInteger operationsCount = new AtomicInteger();
         ElasticsearchUtil.scrollWith(searchRequest, this.esClient, (searchHits) -> {
            try {
               List processInstanceKeys = CollectionUtil.map(searchHits.getHits(), ElasticsearchUtil.searchHitIdToLong);
               operationsCount.addAndGet(this.persistOperations(processInstanceKeys, batchOperation.getId(), batchOperationRequest.getOperationType(), (String)null));
            } catch (PersistenceException var6) {
               throw new RuntimeException(var6);
            }
         }, (Consumer)null, (searchHits) -> {
            this.validateTotalHits(searchHits);
            batchOperation.setInstancesCount((int)searchHits.getTotalHits().value);
         });
         batchOperation.setOperationsTotalCount(operationsCount.get());
         if (operationsCount.get() == 0) {
            batchOperation.setEndDate(OffsetDateTime.now());
         }

         this.persistBatchOperationEntity(batchOperation);
         return batchOperation;
      } catch (Exception var8) {
         throw new OperateRuntimeException(String.format("Exception occurred, while scheduling operation: %s", var8.getMessage()), var8);
      }
   }

   public BatchOperationEntity scheduleSingleOperation(long processInstanceKey, CreateOperationRequestDto operationRequest) {
      logger.debug("Creating operation: processInstanceKey [{}], operation type [{}]", processInstanceKey, operationRequest.getOperationType());

      try {
         BatchOperationEntity batchOperation = this.createBatchOperationEntity(operationRequest.getOperationType(), operationRequest.getName());
         BulkRequest bulkRequest = new BulkRequest();
         int operationsCount = 0;
         String noOperationsReason = null;
         OperationType operationType = operationRequest.getOperationType();
         if (operationType.equals(OperationType.RESOLVE_INCIDENT) && operationRequest.getIncidentId() == null) {
            List allIncidents = this.incidentReader.getAllIncidentsByProcessInstanceKey(processInstanceKey);
            if (allIncidents.size() == 0) {
               batchOperation.setEndDate(OffsetDateTime.now());
               noOperationsReason = "No incidents found.";
            } else {
               for(Iterator var10 = allIncidents.iterator(); var10.hasNext(); ++operationsCount) {
                  IncidentEntity incident = (IncidentEntity)var10.next();
                  bulkRequest.add(this.getIndexOperationRequest(processInstanceKey, incident.getKey(), batchOperation.getId(), operationType));
               }
            }
         } else if (Set.of(OperationType.UPDATE_VARIABLE, OperationType.ADD_VARIABLE).contains(operationType)) {
            bulkRequest.add(this.getIndexVariableOperationRequest(processInstanceKey, ConversionUtils.toLongOrNull(operationRequest.getVariableScopeId()), operationType, operationRequest.getVariableName(), operationRequest.getVariableValue(), batchOperation.getId()));
            ++operationsCount;
         } else {
            bulkRequest.add(this.getIndexOperationRequest(processInstanceKey, ConversionUtils.toLongOrNull(operationRequest.getIncidentId()), batchOperation.getId(), operationType));
            ++operationsCount;
         }

         bulkRequest.add(this.getUpdateProcessInstanceRequest(processInstanceKey, this.getListViewIndicesForProcessInstances(List.of(processInstanceKey)), batchOperation.getId()));
         batchOperation.setOperationsTotalCount(operationsCount);
         batchOperation.setInstancesCount(1);
         bulkRequest.add(this.getIndexBatchOperationRequest(batchOperation));
         ElasticsearchUtil.processBulkRequest(this.esClient, bulkRequest);
         return batchOperation;
      } catch (Exception var12) {
         throw new OperateRuntimeException(String.format("Exception occurred, while scheduling operation: %s", var12.getMessage()), var12);
      }
   }

   private Script getUpdateBatchOperationIdScript(String batchOperationId) {
      Map paramsMap = Map.of("batchOperationId", batchOperationId);
      String script = "if (ctx._source.batchOperationIds == null){ctx._source.batchOperationIds = new String[]{params.batchOperationId};} else {ctx._source.batchOperationIds.add(params.batchOperationId);}";
      return new Script(ScriptType.INLINE, "painless", "if (ctx._source.batchOperationIds == null){ctx._source.batchOperationIds = new String[]{params.batchOperationId};} else {ctx._source.batchOperationIds.add(params.batchOperationId);}", paramsMap);
   }

   private BatchOperationEntity createBatchOperationEntity(OperationType operationType, String name) {
      BatchOperationEntity batchOperationEntity = new BatchOperationEntity();
      batchOperationEntity.generateId();
      batchOperationEntity.setType(operationType);
      batchOperationEntity.setName(name);
      batchOperationEntity.setStartDate(OffsetDateTime.now());
      batchOperationEntity.setUsername(this.userService.getCurrentUser().getUsername());
      return batchOperationEntity;
   }

   private String persistBatchOperationEntity(BatchOperationEntity batchOperationEntity) throws PersistenceException {
      try {
         IndexRequest indexRequest = this.getIndexBatchOperationRequest(batchOperationEntity);
         this.esClient.index(indexRequest, RequestOptions.DEFAULT);
      } catch (IOException var3) {
         logger.error("Error persisting batch operation", var3);
         throw new PersistenceException(String.format("Error persisting batch operation of type [%s]", batchOperationEntity.getType()), var3);
      }

      return batchOperationEntity.getId();
   }

   private IndexRequest getIndexBatchOperationRequest(BatchOperationEntity batchOperationEntity) throws JsonProcessingException {
      return (new IndexRequest(this.batchOperationTemplate.getFullQualifiedName())).id(batchOperationEntity.getId()).source(this.objectMapper.writeValueAsString(batchOperationEntity), XContentType.JSON);
   }

   private int persistOperations(List processInstanceKeys, String batchOperationId, OperationType operationType, String incidentId) throws PersistenceException {
      BulkRequest bulkRequest = new BulkRequest();
      int operationsCount = 0;
      Map incidentKeys = new HashMap();
      if (operationType.equals(OperationType.RESOLVE_INCIDENT) && incidentId == null) {
         incidentKeys = this.incidentReader.getIncidentKeysPerProcessInstance(processInstanceKeys);
      }

      Map processInstanceIdToIndexName = null;

      try {
         processInstanceIdToIndexName = this.getListViewIndicesForProcessInstances(processInstanceKeys);
      } catch (IOException var14) {
         throw new NotFoundException("Couldn't find index names for process instances.", var14);
      }

      Long processInstanceKey;
      for(Iterator var9 = processInstanceKeys.iterator(); var9.hasNext(); bulkRequest.add(this.getUpdateProcessInstanceRequest(processInstanceKey, processInstanceIdToIndexName, batchOperationId))) {
         processInstanceKey = (Long)var9.next();
         if (operationType.equals(OperationType.RESOLVE_INCIDENT) && incidentId == null) {
            List allIncidentKeys = (List)((Map)incidentKeys).get(processInstanceKey);
            if (allIncidentKeys != null && allIncidentKeys.size() != 0) {
               for(Iterator var12 = allIncidentKeys.iterator(); var12.hasNext(); ++operationsCount) {
                  Long incidentKey = (Long)var12.next();
                  bulkRequest.add(this.getIndexOperationRequest(processInstanceKey, incidentKey, batchOperationId, operationType));
               }
            }
         } else {
            bulkRequest.add(this.getIndexOperationRequest(processInstanceKey, ConversionUtils.toLongOrNull(incidentId), batchOperationId, operationType));
            ++operationsCount;
         }
      }

      ElasticsearchUtil.processBulkRequest(this.esClient, bulkRequest);
      return operationsCount;
   }

   private Map getListViewIndicesForProcessInstances(List processInstanceIds) throws IOException {
      List processInstanceIdsAsStrings = CollectionUtil.map(processInstanceIds, Object::toString);
      SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest(this.listViewTemplate, QueryType.ALL);
      searchRequest.source().query(QueryBuilders.idsQuery().addIds(CollectionUtil.toSafeArrayOfStrings(processInstanceIdsAsStrings)));
      Map processInstanceId2IndexName = new HashMap();
      ElasticsearchUtil.scrollWith(searchRequest, this.esClient, (searchHits) -> {
         SearchHit[] var2 = searchHits.getHits();
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            SearchHit searchHit = var2[var4];
            String indexName = searchHit.getIndex();
            Long id = Long.valueOf(searchHit.getId());
            processInstanceId2IndexName.put(id, indexName);
         }

      });
      if (processInstanceId2IndexName.isEmpty()) {
         throw new NotFoundException(String.format("Process instances %s doesn't exists.", processInstanceIds));
      } else {
         return processInstanceId2IndexName;
      }
   }

   private IndexRequest getIndexVariableOperationRequest(Long processInstanceKey, Long scopeKey, OperationType operationType, String name, String value, String batchOperationId) throws PersistenceException {
      OperationEntity operationEntity = this.createOperationEntity(processInstanceKey, operationType, batchOperationId);
      operationEntity.setScopeKey(scopeKey);
      operationEntity.setVariableName(name);
      operationEntity.setVariableValue(value);
      return this.createIndexRequest(operationEntity, processInstanceKey);
   }

   private IndexRequest getIndexOperationRequest(Long processInstanceKey, Long incidentKey, String batchOperationId, OperationType operationType) throws PersistenceException {
      OperationEntity operationEntity = this.createOperationEntity(processInstanceKey, operationType, batchOperationId);
      operationEntity.setIncidentKey(incidentKey);
      return this.createIndexRequest(operationEntity, processInstanceKey);
   }

   private UpdateRequest getUpdateProcessInstanceRequest(Long processInstanceKey, Map processInstanceIdToIndexName, String batchOperationId) {
      String processInstanceId = String.valueOf(processInstanceKey);
      String indexForProcessInstance = (String)CollectionUtil.getOrDefaultForNullValue(processInstanceIdToIndexName, processInstanceKey, this.listViewTemplate.getFullQualifiedName());
      return ((UpdateRequest)(new UpdateRequest()).index(indexForProcessInstance)).id(processInstanceId).script(this.getUpdateBatchOperationIdScript(batchOperationId)).retryOnConflict(3);
   }

   private OperationEntity createOperationEntity(Long processInstanceKey, OperationType operationType, String batchOperationId) {
      OperationEntity operationEntity = new OperationEntity();
      operationEntity.generateId();
      operationEntity.setProcessInstanceKey(processInstanceKey);
      operationEntity.setType(operationType);
      operationEntity.setState(OperationState.SCHEDULED);
      operationEntity.setBatchOperationId(batchOperationId);
      operationEntity.setUsername(this.userService.getCurrentUser().getUsername());
      return operationEntity;
   }

   private IndexRequest createIndexRequest(OperationEntity operationEntity, Long processInstanceKey) throws PersistenceException {
      try {
         return (new IndexRequest(this.operationTemplate.getFullQualifiedName())).id(operationEntity.getId()).source(this.objectMapper.writeValueAsString(operationEntity), XContentType.JSON);
      } catch (IOException var4) {
         throw new PersistenceException(String.format("Error preparing the query to insert operation [%s] for process instance id [%s]", operationEntity.getType(), processInstanceKey), var4);
      }
   }

   private void validateTotalHits(SearchHits hits) {
      long totalHits = hits.getTotalHits().value;
      if (this.operateProperties.getBatchOperationMaxSize() != null && totalHits > this.operateProperties.getBatchOperationMaxSize()) {
         throw new InvalidRequestException(String.format("Too many process instances are selected for batch operation. Maximum possible amount: %s", this.operateProperties.getBatchOperationMaxSize()));
      }
   }
}
