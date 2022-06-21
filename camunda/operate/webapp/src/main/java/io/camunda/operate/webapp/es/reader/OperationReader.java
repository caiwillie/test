package io.camunda.operate.webapp.es.reader;

import io.camunda.operate.entities.BatchOperationEntity;
import io.camunda.operate.entities.OperationEntity;
import io.camunda.operate.entities.OperationState;
import io.camunda.operate.entities.OperationType;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.schema.templates.BatchOperationTemplate;
import io.camunda.operate.schema.templates.OperationTemplate;
import io.camunda.operate.util.CollectionUtil;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.util.ElasticsearchUtil.QueryType;
import io.camunda.operate.webapp.rest.dto.DtoCreator;
import io.camunda.operate.webapp.rest.dto.OperationDto;
import io.camunda.operate.webapp.security.UserService;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.ConstantScoreQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OperationReader extends AbstractReader {
   private static final Logger logger = LoggerFactory.getLogger(OperationReader.class);
   private static final String SCHEDULED_OPERATION;
   private static final String LOCKED_OPERATION;
   @Autowired
   private OperationTemplate operationTemplate;
   @Autowired
   private BatchOperationTemplate batchOperationTemplate;
   @Autowired
   private DateTimeFormatter dateTimeFormatter;
   @Autowired
   private UserService userService;

   public List acquireOperations(int batchSize) {
      TermQueryBuilder scheduledOperationsQuery = QueryBuilders.termQuery("state", SCHEDULED_OPERATION);
      TermQueryBuilder lockedOperationsQuery = QueryBuilders.termQuery("state", LOCKED_OPERATION);
      RangeQueryBuilder lockExpirationTimeQuery = QueryBuilders.rangeQuery("lockExpirationTime");
      lockExpirationTimeQuery.lte(this.dateTimeFormatter.format(OffsetDateTime.now()));
      QueryBuilder operationsQuery = ElasticsearchUtil.joinWithOr(new QueryBuilder[]{scheduledOperationsQuery, ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{lockedOperationsQuery, lockExpirationTimeQuery})});
      ConstantScoreQueryBuilder constantScoreQuery = QueryBuilders.constantScoreQuery(operationsQuery);
      SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest(this.operationTemplate, QueryType.ONLY_RUNTIME).source((new SearchSourceBuilder()).query(constantScoreQuery).sort("batchOperationId", SortOrder.ASC).from(0).size(batchSize));

      try {
         SearchResponse searchResponse = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
         return ElasticsearchUtil.mapSearchHits(searchResponse.getHits().getHits(), this.objectMapper, OperationEntity.class);
      } catch (IOException var10) {
         String message = String.format("Exception occurred, while acquiring operations for execution: %s", var10.getMessage());
         logger.error(message, var10);
         throw new OperateRuntimeException(message, var10);
      }
   }

   public Map getOperationsPerProcessInstanceKey(List processInstanceKeys) {
      Map result = new HashMap();
      TermsQueryBuilder processInstanceKeysQ = QueryBuilders.termsQuery("processInstanceKey", processInstanceKeys);
      ConstantScoreQueryBuilder query = QueryBuilders.constantScoreQuery(ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{processInstanceKeysQ, this.createUsernameQuery()}));
      SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest(this.operationTemplate, QueryType.ALL).source((new SearchSourceBuilder()).query(query).sort("processInstanceKey", SortOrder.ASC).sort("id", SortOrder.ASC));

      try {
         ElasticsearchUtil.scroll(searchRequest, OperationEntity.class, this.objectMapper, this.esClient, (hits) -> {
            List operationEntities = ElasticsearchUtil.mapSearchHits(hits.getHits(), this.objectMapper, OperationEntity.class);
            Iterator var4 = operationEntities.iterator();

            while(var4.hasNext()) {
               OperationEntity operationEntity = (OperationEntity)var4.next();
               CollectionUtil.addToMap(result, operationEntity.getProcessInstanceKey(), operationEntity);
            }

         }, (Consumer)null);
         return result;
      } catch (IOException var8) {
         String message = String.format("Exception occurred, while obtaining operations per process instance id: %s", var8.getMessage());
         logger.error(message, var8);
         throw new OperateRuntimeException(message, var8);
      }
   }

   private QueryBuilder createUsernameQuery() {
      return QueryBuilders.termQuery("username", this.userService.getCurrentUser().getUsername());
   }

   public Map getOperationsPerIncidentKey(String processInstanceId) {
      Map result = new HashMap();
      TermQueryBuilder processInstanceKeysQ = QueryBuilders.termQuery("processInstanceKey", processInstanceId);
      ConstantScoreQueryBuilder query = QueryBuilders.constantScoreQuery(ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{processInstanceKeysQ, this.createUsernameQuery()}));
      SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest(this.operationTemplate, QueryType.ONLY_RUNTIME).source((new SearchSourceBuilder()).query(query).sort("incidentKey", SortOrder.ASC).sort("id", SortOrder.ASC));

      try {
         ElasticsearchUtil.scroll(searchRequest, OperationEntity.class, this.objectMapper, this.esClient, (hits) -> {
            List operationEntities = ElasticsearchUtil.mapSearchHits(hits.getHits(), this.objectMapper, OperationEntity.class);
            Iterator var4 = operationEntities.iterator();

            while(var4.hasNext()) {
               OperationEntity operationEntity = (OperationEntity)var4.next();
               CollectionUtil.addToMap(result, operationEntity.getIncidentKey(), operationEntity);
            }

         }, (Consumer)null);
         return result;
      } catch (IOException var8) {
         String message = String.format("Exception occurred, while obtaining operations per incident id: %s", var8.getMessage());
         logger.error(message, var8);
         throw new OperateRuntimeException(message, var8);
      }
   }

   public Map getUpdateOperationsPerVariableName(Long processInstanceKey, Long scopeKey) {
      Map result = new HashMap();
      TermQueryBuilder processInstanceKeyQuery = QueryBuilders.termQuery("processInstanceKey", processInstanceKey);
      TermQueryBuilder scopeKeyQuery = QueryBuilders.termQuery("scopeKey", scopeKey);
      TermQueryBuilder operationTypeQ = QueryBuilders.termQuery("type", OperationType.UPDATE_VARIABLE.name());
      ConstantScoreQueryBuilder query = QueryBuilders.constantScoreQuery(ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{processInstanceKeyQuery, scopeKeyQuery, operationTypeQ, this.createUsernameQuery()}));
      SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest(this.operationTemplate, QueryType.ALL).source((new SearchSourceBuilder()).query(query).sort("id", SortOrder.ASC));

      try {
         ElasticsearchUtil.scroll(searchRequest, OperationEntity.class, this.objectMapper, this.esClient, (hits) -> {
            List operationEntities = ElasticsearchUtil.mapSearchHits(hits.getHits(), this.objectMapper, OperationEntity.class);
            Iterator var4 = operationEntities.iterator();

            while(var4.hasNext()) {
               OperationEntity operationEntity = (OperationEntity)var4.next();
               CollectionUtil.addToMap(result, operationEntity.getVariableName(), operationEntity);
            }

         }, (Consumer)null);
         return result;
      } catch (IOException var11) {
         String message = String.format("Exception occurred, while obtaining operations per variable name: %s", var11.getMessage());
         logger.error(message, var11);
         throw new OperateRuntimeException(message, var11);
      }
   }

   public List getOperationsByProcessInstanceKey(Long processInstanceKey) {
      TermQueryBuilder processInstanceQ = processInstanceKey == null ? null : QueryBuilders.termQuery("processInstanceKey", processInstanceKey);
      QueryBuilder query = QueryBuilders.constantScoreQuery(ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{processInstanceQ, this.createUsernameQuery()}));
      SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest(this.operationTemplate, QueryType.ALL).source((new SearchSourceBuilder()).query(query).sort("id", SortOrder.ASC));

      try {
         return ElasticsearchUtil.scroll(searchRequest, OperationEntity.class, this.objectMapper, this.esClient);
      } catch (IOException var7) {
         String message = String.format("Exception occurred, while obtaining operations: %s", var7.getMessage());
         logger.error(message, var7);
         throw new OperateRuntimeException(message, var7);
      }
   }

   public List getBatchOperations(int pageSize) {
      String username = this.userService.getCurrentUser().getUsername();
      TermQueryBuilder isOfCurrentUser = QueryBuilders.termQuery("username", username);
      SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest(this.batchOperationTemplate, QueryType.ALL).source((new SearchSourceBuilder()).query(QueryBuilders.constantScoreQuery(isOfCurrentUser)).size(pageSize));

      try {
         return ElasticsearchUtil.mapSearchHits(this.esClient.search(searchRequest, RequestOptions.DEFAULT).getHits().getHits(), this.objectMapper, BatchOperationEntity.class);
      } catch (IOException var7) {
         String message = String.format("Exception occurred, while obtaining batch operations: %s", var7.getMessage());
         throw new OperateRuntimeException(message, var7);
      }
   }

   public List getOperationsByBatchOperationId(String batchOperationId) {
      TermQueryBuilder operationIdQ = QueryBuilders.termQuery("batchOperationId", batchOperationId);
      SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest(this.operationTemplate, QueryType.ALL).source((new SearchSourceBuilder()).query(operationIdQ));

      try {
         List operationEntities = ElasticsearchUtil.scroll(searchRequest, OperationEntity.class, this.objectMapper, this.esClient);
         return DtoCreator.create(operationEntities, OperationDto.class);
      } catch (IOException var6) {
         String message = String.format("Exception occurred, while searching for operation with batchOperationId: %s", var6.getMessage());
         logger.error(message, var6);
         throw new OperateRuntimeException(message, var6);
      }
   }

   public List getOperations(OperationType operationType, String processInstanceId, String scopeId, String variableName) {
      TermQueryBuilder operationTypeQ = QueryBuilders.termQuery("type", operationType);
      TermQueryBuilder processInstanceKeyQ = QueryBuilders.termQuery("processInstanceKey", processInstanceId);
      TermQueryBuilder scopeKeyQ = QueryBuilders.termQuery("scopeKey", scopeId);
      TermQueryBuilder variableNameQ = QueryBuilders.termQuery("variableName", variableName);
      SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest(this.operationTemplate, QueryType.ALL).source((new SearchSourceBuilder()).query(ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{operationTypeQ, processInstanceKeyQ, scopeKeyQ, variableNameQ})));

      try {
         List operationEntities = ElasticsearchUtil.scroll(searchRequest, OperationEntity.class, this.objectMapper, this.esClient);
         return DtoCreator.create(operationEntities, OperationDto.class);
      } catch (IOException var12) {
         String message = String.format("Exception occurred, while searching for operation.", var12.getMessage());
         logger.error(message, var12);
         throw new OperateRuntimeException(message, var12);
      }
   }

   static {
      SCHEDULED_OPERATION = OperationState.SCHEDULED.toString();
      LOCKED_OPERATION = OperationState.LOCKED.toString();
   }
}
