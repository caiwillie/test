/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.core.JsonProcessingException
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  io.camunda.operate.entities.BatchOperationEntity
 *  io.camunda.operate.entities.IncidentEntity
 *  io.camunda.operate.entities.OperationEntity
 *  io.camunda.operate.entities.OperationState
 *  io.camunda.operate.entities.OperationType
 *  io.camunda.operate.exceptions.OperateRuntimeException
 *  io.camunda.operate.exceptions.PersistenceException
 *  io.camunda.operate.property.OperateProperties
 *  io.camunda.operate.schema.templates.BatchOperationTemplate
 *  io.camunda.operate.schema.templates.ListViewTemplate
 *  io.camunda.operate.schema.templates.OperationTemplate
 *  io.camunda.operate.schema.templates.TemplateDescriptor
 *  io.camunda.operate.util.CollectionUtil
 *  io.camunda.operate.util.ConversionUtils
 *  io.camunda.operate.util.ElasticsearchUtil
 *  io.camunda.operate.util.ElasticsearchUtil$QueryType
 *  io.camunda.operate.webapp.es.reader.IncidentReader
 *  io.camunda.operate.webapp.es.reader.ListViewReader
 *  io.camunda.operate.webapp.es.reader.OperationReader
 *  io.camunda.operate.webapp.rest.dto.operation.CreateBatchOperationRequestDto
 *  io.camunda.operate.webapp.rest.dto.operation.CreateOperationRequestDto
 *  io.camunda.operate.webapp.rest.exception.InvalidRequestException
 *  io.camunda.operate.webapp.rest.exception.NotFoundException
 *  io.camunda.operate.webapp.security.UserService
 *  org.elasticsearch.action.bulk.BulkRequest
 *  org.elasticsearch.action.index.IndexRequest
 *  org.elasticsearch.action.search.SearchRequest
 *  org.elasticsearch.action.support.WriteRequest$RefreshPolicy
 *  org.elasticsearch.action.update.UpdateRequest
 *  org.elasticsearch.client.RequestOptions
 *  org.elasticsearch.client.RestHighLevelClient
 *  org.elasticsearch.index.query.ConstantScoreQueryBuilder
 *  org.elasticsearch.index.query.QueryBuilder
 *  org.elasticsearch.index.query.QueryBuilders
 *  org.elasticsearch.script.Script
 *  org.elasticsearch.script.ScriptType
 *  org.elasticsearch.search.SearchHit
 *  org.elasticsearch.search.SearchHits
 *  org.elasticsearch.search.builder.SearchSourceBuilder
 *  org.elasticsearch.xcontent.XContentType
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 */
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
import io.camunda.operate.schema.templates.TemplateDescriptor;
import io.camunda.operate.util.CollectionUtil;
import io.camunda.operate.util.ConversionUtils;
import io.camunda.operate.util.ElasticsearchUtil;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.ConstantScoreQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
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

    public List<OperationEntity> lockBatch() throws PersistenceException {
        String workerId = this.operateProperties.getOperationExecutor().getWorkerId();
        long lockTimeout = this.operateProperties.getOperationExecutor().getLockTimeout();
        int batchSize = this.operateProperties.getOperationExecutor().getBatchSize();
        List operationEntities = this.operationReader.acquireOperations(batchSize);
        BulkRequest bulkRequest = new BulkRequest();
        Iterator iterator = operationEntities.iterator();
        while (true) {
            if (!iterator.hasNext()) {
                ElasticsearchUtil.processBulkRequest((RestHighLevelClient)this.esClient, (BulkRequest)bulkRequest, (boolean)true);
                logger.debug("{} operations locked", (Object)operationEntities.size());
                return operationEntities;
            }
            OperationEntity operation = (OperationEntity)iterator.next();
            operation.setState(OperationState.LOCKED);
            operation.setLockOwner(workerId);
            operation.setLockExpirationTime(OffsetDateTime.now().plus(lockTimeout, ChronoUnit.MILLIS));
            bulkRequest.add(this.createUpdateByIdRequest(operation, false));
        }
    }

    private UpdateRequest createUpdateByIdRequest(OperationEntity operation, boolean refreshImmediately) throws PersistenceException {
        try {
            Map jsonMap = (Map)this.objectMapper.readValue(this.objectMapper.writeValueAsString((Object)operation), HashMap.class);
            UpdateRequest updateRequest = ((UpdateRequest)new UpdateRequest().index(this.operationTemplate.getFullQualifiedName())).id(operation.getId()).doc(jsonMap);
            if (!refreshImmediately) return updateRequest;
            updateRequest = updateRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            return updateRequest;
        }
        catch (IOException e) {
            throw new PersistenceException(String.format("Error preparing the query to update operation [%s] for process instance id [%s]", operation.getId(), operation.getProcessInstanceKey()), (Throwable)e);
        }
    }

    public void updateOperation(OperationEntity operation) throws PersistenceException {
        UpdateRequest updateRequest = this.createUpdateByIdRequest(operation, true);
        ElasticsearchUtil.executeUpdate((RestHighLevelClient)this.esClient, (UpdateRequest)updateRequest);
    }

    public BatchOperationEntity scheduleBatchOperation(CreateBatchOperationRequestDto batchOperationRequest) {
        logger.debug("Creating batch operation: operationRequest [{}]", (Object)batchOperationRequest);
        try {
            BatchOperationEntity batchOperation = this.createBatchOperationEntity(batchOperationRequest.getOperationType(), batchOperationRequest.getName());
            int batchSize = this.operateProperties.getElasticsearch().getBatchSize();
            ConstantScoreQueryBuilder query = this.listViewReader.createProcessInstancesQuery(batchOperationRequest.getQuery());
            ElasticsearchUtil.QueryType queryType = ElasticsearchUtil.QueryType.ONLY_RUNTIME;
            if (batchOperationRequest.getOperationType().equals((Object)OperationType.DELETE_PROCESS_INSTANCE)) {
                queryType = ElasticsearchUtil.QueryType.ALL;
            }
            SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.listViewTemplate, (ElasticsearchUtil.QueryType)queryType).source(new SearchSourceBuilder().query((QueryBuilder)query).size(batchSize).fetchSource(false));
            AtomicInteger operationsCount = new AtomicInteger();
            ElasticsearchUtil.scrollWith((SearchRequest)searchRequest, (RestHighLevelClient)this.esClient, searchHits -> {
                try {
                    List processInstanceKeys = CollectionUtil.map((Object[])searchHits.getHits(), (Function)ElasticsearchUtil.searchHitIdToLong);
                    operationsCount.addAndGet(this.persistOperations(processInstanceKeys, batchOperation.getId(), batchOperationRequest.getOperationType(), null));
                }
                catch (PersistenceException e) {
                    throw new RuntimeException(e);
                }
            }, null, searchHits -> {
                this.validateTotalHits((SearchHits)searchHits);
                batchOperation.setInstancesCount(Integer.valueOf((int)searchHits.getTotalHits().value));
            });
            batchOperation.setOperationsTotalCount(Integer.valueOf(operationsCount.get()));
            if (operationsCount.get() == 0) {
                batchOperation.setEndDate(OffsetDateTime.now());
            }
            this.persistBatchOperationEntity(batchOperation);
            return batchOperation;
        }
        catch (Exception ex) {
            throw new OperateRuntimeException(String.format("Exception occurred, while scheduling operation: %s", ex.getMessage()), (Throwable)ex);
        }
    }

    public BatchOperationEntity scheduleSingleOperation(long processInstanceKey, CreateOperationRequestDto operationRequest) {
        logger.debug("Creating operation: processInstanceKey [{}], operation type [{}]", (Object)processInstanceKey, (Object)operationRequest.getOperationType());
        try {
            BatchOperationEntity batchOperation = this.createBatchOperationEntity(operationRequest.getOperationType(), operationRequest.getName());
            BulkRequest bulkRequest = new BulkRequest();
            int operationsCount = 0;
            String noOperationsReason = null;
            OperationType operationType = operationRequest.getOperationType();
            if (operationType.equals((Object)OperationType.RESOLVE_INCIDENT) && operationRequest.getIncidentId() == null) {
                List<IncidentEntity> allIncidents = this.incidentReader.getAllIncidentsByProcessInstanceKey(Long.valueOf(processInstanceKey));
                if (allIncidents.size() == 0) {
                    batchOperation.setEndDate(OffsetDateTime.now());
                    noOperationsReason = "No incidents found.";
                } else {
                    for (IncidentEntity incident : allIncidents) {
                        bulkRequest.add(this.getIndexOperationRequest(processInstanceKey, incident.getKey(), batchOperation.getId(), operationType));
                        ++operationsCount;
                    }
                }
            } else if (Set.of(OperationType.UPDATE_VARIABLE, OperationType.ADD_VARIABLE).contains(operationType)) {
                bulkRequest.add(this.getIndexVariableOperationRequest(processInstanceKey, ConversionUtils.toLongOrNull((String)operationRequest.getVariableScopeId()), operationType, operationRequest.getVariableName(), operationRequest.getVariableValue(), batchOperation.getId()));
                ++operationsCount;
            } else {
                bulkRequest.add(this.getIndexOperationRequest(processInstanceKey, ConversionUtils.toLongOrNull((String)operationRequest.getIncidentId()), batchOperation.getId(), operationType));
                ++operationsCount;
            }
            bulkRequest.add(this.getUpdateProcessInstanceRequest(processInstanceKey, this.getListViewIndicesForProcessInstances(List.of(Long.valueOf(processInstanceKey))), batchOperation.getId()));
            batchOperation.setOperationsTotalCount(Integer.valueOf(operationsCount));
            batchOperation.setInstancesCount(Integer.valueOf(1));
            bulkRequest.add(this.getIndexBatchOperationRequest(batchOperation));
            ElasticsearchUtil.processBulkRequest((RestHighLevelClient)this.esClient, (BulkRequest)bulkRequest);
            return batchOperation;
        }
        catch (Exception ex) {
            throw new OperateRuntimeException(String.format("Exception occurred, while scheduling operation: %s", ex.getMessage()), (Throwable)ex);
        }
    }

    private Script getUpdateBatchOperationIdScript(String batchOperationId) {
        Map<String, Object> paramsMap = Map.of("batchOperationId", batchOperationId);
        String script = "if (ctx._source.batchOperationIds == null){ctx._source.batchOperationIds = new String[]{params.batchOperationId};} else {ctx._source.batchOperationIds.add(params.batchOperationId);}";
        return new Script(ScriptType.INLINE,
                "painless",
                "if (ctx._source.batchOperationIds == null){ctx._source.batchOperationIds = new String[]{params.batchOperationId};} else {ctx._source.batchOperationIds.add(params.batchOperationId);}",
                paramsMap);
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
        }
        catch (IOException e) {
            logger.error("Error persisting batch operation", e);
            throw new PersistenceException(String.format("Error persisting batch operation of type [%s]", batchOperationEntity.getType()), (Throwable)e);
        }
        return batchOperationEntity.getId();
    }

    private IndexRequest getIndexBatchOperationRequest(BatchOperationEntity batchOperationEntity) throws JsonProcessingException {
        return new IndexRequest(this.batchOperationTemplate.getFullQualifiedName()).id(batchOperationEntity.getId()).source(this.objectMapper.writeValueAsString((Object)batchOperationEntity), XContentType.JSON);
    }

    private int persistOperations(List<Long> processInstanceKeys, String batchOperationId, OperationType operationType, String incidentId) throws PersistenceException {
        BulkRequest bulkRequest = new BulkRequest();
        int operationsCount = 0;
        Map incidentKeys = new HashMap();
        if (operationType.equals((Object)OperationType.RESOLVE_INCIDENT) && incidentId == null) {
            incidentKeys = this.incidentReader.getIncidentKeysPerProcessInstance(processInstanceKeys);
        }
        Map<Long, String> processInstanceIdToIndexName = null;
        try {
            processInstanceIdToIndexName = this.getListViewIndicesForProcessInstances(processInstanceKeys);
        }
        catch (IOException e) {
            throw new NotFoundException("Couldn't find index names for process instances.", (Throwable)e);
        }
        Iterator<Long> iterator = processInstanceKeys.iterator();
        while (true) {
            if (!iterator.hasNext()) {
                ElasticsearchUtil.processBulkRequest((RestHighLevelClient)this.esClient, (BulkRequest)bulkRequest);
                return operationsCount;
            }
            Long processInstanceKey = iterator.next();
            if (operationType.equals((Object)OperationType.RESOLVE_INCIDENT) && incidentId == null) {
                List<Long> allIncidentKeys = (List)incidentKeys.get(processInstanceKey);
                if (allIncidentKeys != null && allIncidentKeys.size() != 0) {
                    for (Long incidentKey : allIncidentKeys) {
                        bulkRequest.add(this.getIndexOperationRequest(processInstanceKey, incidentKey, batchOperationId, operationType));
                        ++operationsCount;
                    }
                }
            } else {
                bulkRequest.add(this.getIndexOperationRequest(processInstanceKey, ConversionUtils.toLongOrNull((String)incidentId), batchOperationId, operationType));
                ++operationsCount;
            }
            bulkRequest.add(this.getUpdateProcessInstanceRequest(processInstanceKey, processInstanceIdToIndexName, batchOperationId));
        }
    }

    private Map<Long, String> getListViewIndicesForProcessInstances(List<Long> processInstanceIds) throws IOException {
        List processInstanceIdsAsStrings = CollectionUtil.map(processInstanceIds, Object::toString);
        SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.listViewTemplate, (ElasticsearchUtil.QueryType)ElasticsearchUtil.QueryType.ALL);
        searchRequest.source().query((QueryBuilder)QueryBuilders.idsQuery().addIds(CollectionUtil.toSafeArrayOfStrings((Collection)processInstanceIdsAsStrings)));
        HashMap<Long, String> processInstanceId2IndexName = new HashMap<Long, String>();
        ElasticsearchUtil.scrollWith((SearchRequest)searchRequest, (RestHighLevelClient)this.esClient, searchHits -> {
            SearchHit[] searchHitArray = searchHits.getHits();
            int n = searchHitArray.length;
            int n2 = 0;
            while (n2 < n) {
                SearchHit searchHit = searchHitArray[n2];
                String indexName = searchHit.getIndex();
                Long id = Long.valueOf(searchHit.getId());
                processInstanceId2IndexName.put(id, indexName);
                ++n2;
            }
        });
        if (!processInstanceId2IndexName.isEmpty()) return processInstanceId2IndexName;
        throw new NotFoundException(String.format("Process instances %s doesn't exists.", processInstanceIds));
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

    private UpdateRequest getUpdateProcessInstanceRequest(Long processInstanceKey, Map<Long, String> processInstanceIdToIndexName, String batchOperationId) {
        String processInstanceId = String.valueOf(processInstanceKey);
        String indexForProcessInstance = (String)CollectionUtil.getOrDefaultForNullValue(processInstanceIdToIndexName, processInstanceKey, this.listViewTemplate.getFullQualifiedName());
        return ((UpdateRequest)new UpdateRequest().index(indexForProcessInstance)).id(processInstanceId).script(this.getUpdateBatchOperationIdScript(batchOperationId)).retryOnConflict(3);
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
            return new IndexRequest(this.operationTemplate.getFullQualifiedName()).id(operationEntity.getId()).source(this.objectMapper.writeValueAsString((Object)operationEntity), XContentType.JSON);
        }
        catch (IOException e) {
            throw new PersistenceException(String.format("Error preparing the query to insert operation [%s] for process instance id [%s]", operationEntity.getType(), processInstanceKey), (Throwable)e);
        }
    }

    private void validateTotalHits(SearchHits hits) {
        long totalHits = hits.getTotalHits().value;
        if (this.operateProperties.getBatchOperationMaxSize() == null) return;
        if (totalHits <= this.operateProperties.getBatchOperationMaxSize()) return;
        throw new InvalidRequestException(String.format("Too many process instances are selected for batch operation. Maximum possible amount: %s", this.operateProperties.getBatchOperationMaxSize()));
    }
}
