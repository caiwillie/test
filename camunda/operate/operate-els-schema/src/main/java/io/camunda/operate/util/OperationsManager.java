package io.camunda.operate.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.entities.OperationEntity;
import io.camunda.operate.entities.OperationState;
import io.camunda.operate.entities.OperationType;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.exceptions.PersistenceException;
import io.camunda.operate.schema.templates.BatchOperationTemplate;
import io.camunda.operate.schema.templates.OperationTemplate;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OperationsManager {
   private static final Logger logger = LoggerFactory.getLogger(OperationsManager.class);
   @Autowired
   private BatchOperationTemplate batchOperationTemplate;
   @Autowired
   private OperationTemplate operationTemplate;
   @Autowired
   private ObjectMapper objectMapper;
   @Autowired
   private RestHighLevelClient esClient;

   public void updateFinishedInBatchOperation(String batchOperationId) throws PersistenceException {
      this.updateFinishedInBatchOperation(batchOperationId, (BulkRequest)null);
   }

   public void updateFinishedInBatchOperation(String batchOperationId, BulkRequest bulkRequest) throws PersistenceException {
      UpdateRequest updateRequest = ((UpdateRequest)(new UpdateRequest()).index(this.batchOperationTemplate.getFullQualifiedName())).id(batchOperationId).script(this.getIncrementFinishedScript()).retryOnConflict(3);
      if (bulkRequest == null) {
         ElasticsearchUtil.executeUpdate(this.esClient, updateRequest);
      } else {
         bulkRequest.add(updateRequest);
      }

   }

   private Script getIncrementFinishedScript() throws PersistenceException {
      try {
         Map paramsMap = new HashMap();
         paramsMap.put("now", OffsetDateTime.now());
         Map jsonMap = (Map)this.objectMapper.readValue(this.objectMapper.writeValueAsString(paramsMap), HashMap.class);
         String script = "ctx._source.operationsFinishedCount += 1;if (ctx._source.operationsFinishedCount == ctx._source.operationsTotalCount)    ctx._source.endDate = params.now;";
         return new Script(ScriptType.INLINE, "painless", script, jsonMap);
      } catch (IOException var4) {
         throw new PersistenceException("Error preparing the query to update batch operation", var4);
      }
   }

   public void completeOperation(Long zeebeCommandKey, Long processInstanceKey, Long incidentKey, OperationType operationType, BulkRequest bulkRequest) throws PersistenceException {
      BulkRequest theBulkRequest;
      if (bulkRequest == null) {
         theBulkRequest = new BulkRequest();
      } else {
         theBulkRequest = bulkRequest;
      }

      List operations = this.getOperations(zeebeCommandKey, processInstanceKey, incidentKey, operationType);

      OperationEntity o;
      for(Iterator var8 = operations.iterator(); var8.hasNext(); this.completeOperation(o.getId(), theBulkRequest)) {
         o = (OperationEntity)var8.next();
         if (o.getBatchOperationId() != null) {
            this.updateFinishedInBatchOperation(o.getBatchOperationId(), theBulkRequest);
         }
      }

      if (bulkRequest == null) {
         ElasticsearchUtil.processBulkRequest(this.esClient, theBulkRequest);
      }

   }

   public void completeOperation(OperationEntity operationEntity) throws PersistenceException {
      BulkRequest bulkRequest = new BulkRequest();
      if (operationEntity.getBatchOperationId() != null) {
         this.updateFinishedInBatchOperation(operationEntity.getBatchOperationId(), bulkRequest);
      }

      this.completeOperation(operationEntity.getId(), bulkRequest);
      ElasticsearchUtil.processBulkRequest(this.esClient, bulkRequest);
   }

   public List getOperations(Long zeebeCommandKey, Long processInstanceKey, Long incidentKey, OperationType operationType) {
      if (processInstanceKey == null && zeebeCommandKey == null) {
         throw new OperateRuntimeException("Wrong call to search for operation. Not enough parameters.");
      } else {
         TermQueryBuilder zeebeCommandKeyQ = zeebeCommandKey != null ? QueryBuilders.termQuery("zeebeCommandKey", zeebeCommandKey) : null;
         TermQueryBuilder processInstanceKeyQ = processInstanceKey != null ? QueryBuilders.termQuery("processInstanceKey", processInstanceKey) : null;
         TermQueryBuilder incidentKeyQ = incidentKey != null ? QueryBuilders.termQuery("incidentKey", incidentKey) : null;
         TermQueryBuilder operationTypeQ = operationType != null ? QueryBuilders.termQuery("type", operationType.name()) : null;
         QueryBuilder query = ElasticsearchUtil.joinWithAnd(zeebeCommandKeyQ, processInstanceKeyQ, incidentKeyQ, operationTypeQ, QueryBuilders.termsQuery("state", new String[]{OperationState.SENT.name(), OperationState.LOCKED.name()}));
         SearchRequest searchRequest = (new SearchRequest(new String[]{this.operationTemplate.getAlias()})).source((new SearchSourceBuilder()).query(query).size(1));

         try {
            return ElasticsearchUtil.scroll(searchRequest, OperationEntity.class, this.objectMapper, this.esClient);
         } catch (IOException var13) {
            String message = String.format("Exception occurred, while obtaining the operations: %s", var13.getMessage());
            throw new OperateRuntimeException(message, var13);
         }
      }
   }

   public void completeOperation(String operationId, BulkRequest bulkRequest) {
      UpdateRequest updateRequest = ((UpdateRequest)(new UpdateRequest()).index(this.operationTemplate.getFullQualifiedName())).id(operationId).script(this.getUpdateOperationScript()).retryOnConflict(3);
      bulkRequest.add(updateRequest);
   }

   private Script getUpdateOperationScript() {
      String script = "ctx._source.state = '" + OperationState.COMPLETED.toString() + "';ctx._source.lockOwner = null;ctx._source.lockExpirationTime = null;";
      return new Script(ScriptType.INLINE, "painless", script, Collections.emptyMap());
   }
}
