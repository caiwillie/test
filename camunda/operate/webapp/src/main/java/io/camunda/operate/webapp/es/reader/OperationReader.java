/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  io.camunda.operate.entities.BatchOperationEntity
 *  io.camunda.operate.entities.OperationEntity
 *  io.camunda.operate.entities.OperationState
 *  io.camunda.operate.entities.OperationType
 *  io.camunda.operate.exceptions.OperateRuntimeException
 *  io.camunda.operate.schema.templates.BatchOperationTemplate
 *  io.camunda.operate.schema.templates.OperationTemplate
 *  io.camunda.operate.schema.templates.TemplateDescriptor
 *  io.camunda.operate.util.CollectionUtil
 *  io.camunda.operate.util.ElasticsearchUtil
 *  io.camunda.operate.util.ElasticsearchUtil$QueryType
 *  io.camunda.operate.webapp.es.reader.AbstractReader
 *  io.camunda.operate.webapp.rest.dto.DtoCreator
 *  io.camunda.operate.webapp.rest.dto.OperationDto
 *  io.camunda.operate.webapp.security.UserService
 *  org.elasticsearch.action.search.SearchRequest
 *  org.elasticsearch.action.search.SearchResponse
 *  org.elasticsearch.client.RequestOptions
 *  org.elasticsearch.client.RestHighLevelClient
 *  org.elasticsearch.index.query.ConstantScoreQueryBuilder
 *  org.elasticsearch.index.query.QueryBuilder
 *  org.elasticsearch.index.query.QueryBuilders
 *  org.elasticsearch.index.query.RangeQueryBuilder
 *  org.elasticsearch.index.query.TermQueryBuilder
 *  org.elasticsearch.index.query.TermsQueryBuilder
 *  org.elasticsearch.search.SearchHit
 *  org.elasticsearch.search.builder.SearchSourceBuilder
 *  org.elasticsearch.search.sort.SortOrder
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.es.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.entities.BatchOperationEntity;
import io.camunda.operate.entities.OperationEntity;
import io.camunda.operate.entities.OperationState;
import io.camunda.operate.entities.OperationType;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.schema.templates.BatchOperationTemplate;
import io.camunda.operate.schema.templates.OperationTemplate;
import io.camunda.operate.schema.templates.TemplateDescriptor;
import io.camunda.operate.util.CollectionUtil;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.webapp.es.reader.AbstractReader;
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
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.ConstantScoreQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OperationReader
extends AbstractReader {
    private static final Logger logger = LoggerFactory.getLogger(OperationReader.class);
    private static final String SCHEDULED_OPERATION = OperationState.SCHEDULED.toString();
    private static final String LOCKED_OPERATION = OperationState.LOCKED.toString();
    @Autowired
    private OperationTemplate operationTemplate;
    @Autowired
    private BatchOperationTemplate batchOperationTemplate;
    @Autowired
    private DateTimeFormatter dateTimeFormatter;
    @Autowired
    private UserService userService;

    public List<OperationEntity> acquireOperations(int batchSize) {
        TermQueryBuilder scheduledOperationsQuery = QueryBuilders.termQuery((String)"state", (String)SCHEDULED_OPERATION);
        TermQueryBuilder lockedOperationsQuery = QueryBuilders.termQuery((String)"state", (String)LOCKED_OPERATION);
        RangeQueryBuilder lockExpirationTimeQuery = QueryBuilders.rangeQuery((String)"lockExpirationTime");
        lockExpirationTimeQuery.lte((Object)this.dateTimeFormatter.format(OffsetDateTime.now()));
        QueryBuilder operationsQuery = ElasticsearchUtil.joinWithOr((QueryBuilder[])new QueryBuilder[]{scheduledOperationsQuery, ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{lockedOperationsQuery, lockExpirationTimeQuery})});
        ConstantScoreQueryBuilder constantScoreQuery = QueryBuilders.constantScoreQuery((QueryBuilder)operationsQuery);
        SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.operationTemplate, (ElasticsearchUtil.QueryType)ElasticsearchUtil.QueryType.ONLY_RUNTIME).source(new SearchSourceBuilder().query((QueryBuilder)constantScoreQuery).sort("batchOperationId", SortOrder.ASC).from(0).size(batchSize));
        try {
            SearchResponse searchResponse = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
            return ElasticsearchUtil.mapSearchHits((SearchHit[])searchResponse.getHits().getHits(), (ObjectMapper)this.objectMapper, OperationEntity.class);
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while acquiring operations for execution: %s", e.getMessage());
            logger.error(message, e);
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    public Map<Long, List<OperationEntity>> getOperationsPerProcessInstanceKey(List<Long> processInstanceKeys) {
        HashMap<Long, List<OperationEntity>> result = new HashMap<Long, List<OperationEntity>>();
        TermsQueryBuilder processInstanceKeysQ = QueryBuilders.termsQuery((String)"processInstanceKey", processInstanceKeys);
        ConstantScoreQueryBuilder query = QueryBuilders.constantScoreQuery((QueryBuilder)ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{processInstanceKeysQ, this.createUsernameQuery()}));
        SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.operationTemplate, (ElasticsearchUtil.QueryType)ElasticsearchUtil.QueryType.ALL).source(new SearchSourceBuilder().query((QueryBuilder)query).sort("processInstanceKey", SortOrder.ASC).sort("id", SortOrder.ASC));
        try {
            ElasticsearchUtil.scroll((SearchRequest)searchRequest, OperationEntity.class, (ObjectMapper)this.objectMapper, (RestHighLevelClient)this.esClient, hits -> {
                List operationEntities = ElasticsearchUtil.mapSearchHits((SearchHit[])hits.getHits(), (ObjectMapper)this.objectMapper, OperationEntity.class);
                Iterator iterator = operationEntities.iterator();
                while (iterator.hasNext()) {
                    OperationEntity operationEntity = (OperationEntity)iterator.next();
                    CollectionUtil.addToMap((Map)result, (Object)operationEntity.getProcessInstanceKey(), (Object)operationEntity);
                }
            }, null);
            return result;
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining operations per process instance id: %s", e.getMessage());
            logger.error(message, e);
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    private QueryBuilder createUsernameQuery() {
        return QueryBuilders.termQuery((String)"username", (String)this.userService.getCurrentUser().getUsername());
    }

    public Map<Long, List<OperationEntity>> getOperationsPerIncidentKey(String processInstanceId) {
        HashMap<Long, List<OperationEntity>> result = new HashMap<Long, List<OperationEntity>>();
        TermQueryBuilder processInstanceKeysQ = QueryBuilders.termQuery((String)"processInstanceKey", (String)processInstanceId);
        ConstantScoreQueryBuilder query = QueryBuilders.constantScoreQuery((QueryBuilder)ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{processInstanceKeysQ, this.createUsernameQuery()}));
        SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.operationTemplate, (ElasticsearchUtil.QueryType)ElasticsearchUtil.QueryType.ONLY_RUNTIME).source(new SearchSourceBuilder().query((QueryBuilder)query).sort("incidentKey", SortOrder.ASC).sort("id", SortOrder.ASC));
        try {
            ElasticsearchUtil.scroll((SearchRequest)searchRequest, OperationEntity.class, (ObjectMapper)this.objectMapper, (RestHighLevelClient)this.esClient, hits -> {
                List operationEntities = ElasticsearchUtil.mapSearchHits((SearchHit[])hits.getHits(), (ObjectMapper)this.objectMapper, OperationEntity.class);
                Iterator iterator = operationEntities.iterator();
                while (iterator.hasNext()) {
                    OperationEntity operationEntity = (OperationEntity)iterator.next();
                    CollectionUtil.addToMap((Map)result, (Object)operationEntity.getIncidentKey(), (Object)operationEntity);
                }
            }, null);
            return result;
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining operations per incident id: %s", e.getMessage());
            logger.error(message, e);
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    public Map<String, List<OperationEntity>> getUpdateOperationsPerVariableName(Long processInstanceKey, Long scopeKey) {
        HashMap<String, List<OperationEntity>> result = new HashMap<String, List<OperationEntity>>();
        TermQueryBuilder processInstanceKeyQuery = QueryBuilders.termQuery((String)"processInstanceKey", (Object)processInstanceKey);
        TermQueryBuilder scopeKeyQuery = QueryBuilders.termQuery((String)"scopeKey", (Object)scopeKey);
        TermQueryBuilder operationTypeQ = QueryBuilders.termQuery((String)"type", (String)OperationType.UPDATE_VARIABLE.name());
        ConstantScoreQueryBuilder query = QueryBuilders.constantScoreQuery((QueryBuilder)ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{processInstanceKeyQuery, scopeKeyQuery, operationTypeQ, this.createUsernameQuery()}));
        SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.operationTemplate, (ElasticsearchUtil.QueryType)ElasticsearchUtil.QueryType.ALL).source(new SearchSourceBuilder().query((QueryBuilder)query).sort("id", SortOrder.ASC));
        try {
            ElasticsearchUtil.scroll((SearchRequest)searchRequest, OperationEntity.class, (ObjectMapper)this.objectMapper, (RestHighLevelClient)this.esClient, hits -> {
                List operationEntities = ElasticsearchUtil.mapSearchHits((SearchHit[])hits.getHits(), (ObjectMapper)this.objectMapper, OperationEntity.class);
                Iterator iterator = operationEntities.iterator();
                while (iterator.hasNext()) {
                    OperationEntity operationEntity = (OperationEntity)iterator.next();
                    CollectionUtil.addToMap((Map)result, (Object)operationEntity.getVariableName(), (Object)operationEntity);
                }
            }, null);
            return result;
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining operations per variable name: %s", e.getMessage());
            logger.error(message, e);
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    public List<OperationEntity> getOperationsByProcessInstanceKey(Long processInstanceKey) {
        TermQueryBuilder processInstanceQ = processInstanceKey == null ? null : QueryBuilders.termQuery((String)"processInstanceKey", (Object)processInstanceKey);
        ConstantScoreQueryBuilder query = QueryBuilders.constantScoreQuery((QueryBuilder)ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{processInstanceQ, this.createUsernameQuery()}));
        SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.operationTemplate, (ElasticsearchUtil.QueryType)ElasticsearchUtil.QueryType.ALL).source(new SearchSourceBuilder().query((QueryBuilder)query).sort("id", SortOrder.ASC));
        try {
            return ElasticsearchUtil.scroll((SearchRequest)searchRequest, OperationEntity.class, (ObjectMapper)this.objectMapper, (RestHighLevelClient)this.esClient);
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining operations: %s", e.getMessage());
            logger.error(message, e);
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    public List<BatchOperationEntity> getBatchOperations(int pageSize) {
        String username = this.userService.getCurrentUser().getUsername();
        TermQueryBuilder isOfCurrentUser = QueryBuilders.termQuery((String)"username", (String)username);
        SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.batchOperationTemplate, (ElasticsearchUtil.QueryType)ElasticsearchUtil.QueryType.ALL).source(new SearchSourceBuilder().query((QueryBuilder)QueryBuilders.constantScoreQuery((QueryBuilder)isOfCurrentUser)).size(pageSize));
        try {
            return ElasticsearchUtil.mapSearchHits((SearchHit[])this.esClient.search(searchRequest, RequestOptions.DEFAULT).getHits().getHits(), (ObjectMapper)this.objectMapper, BatchOperationEntity.class);
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining batch operations: %s", e.getMessage());
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    public List<OperationDto> getOperationsByBatchOperationId(String batchOperationId) {
        TermQueryBuilder operationIdQ = QueryBuilders.termQuery((String)"batchOperationId", (String)batchOperationId);
        SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.operationTemplate, (ElasticsearchUtil.QueryType)ElasticsearchUtil.QueryType.ALL).source(new SearchSourceBuilder().query((QueryBuilder)operationIdQ));
        try {
            List operationEntities = ElasticsearchUtil.scroll((SearchRequest)searchRequest, OperationEntity.class, (ObjectMapper)this.objectMapper, (RestHighLevelClient)this.esClient);
            return DtoCreator.create((List)operationEntities, OperationDto.class);
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while searching for operation with batchOperationId: %s", e.getMessage());
            logger.error(message, e);
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    public List<OperationDto> getOperations(OperationType operationType, String processInstanceId, String scopeId, String variableName) {
        TermQueryBuilder operationTypeQ = QueryBuilders.termQuery((String)"type", (Object)operationType);
        TermQueryBuilder processInstanceKeyQ = QueryBuilders.termQuery((String)"processInstanceKey", (String)processInstanceId);
        TermQueryBuilder scopeKeyQ = QueryBuilders.termQuery((String)"scopeKey", (String)scopeId);
        TermQueryBuilder variableNameQ = QueryBuilders.termQuery((String)"variableName", (String)variableName);
        SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.operationTemplate, (ElasticsearchUtil.QueryType)ElasticsearchUtil.QueryType.ALL).source(new SearchSourceBuilder().query(ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{operationTypeQ, processInstanceKeyQ, scopeKeyQ, variableNameQ})));
        try {
            List operationEntities = ElasticsearchUtil.scroll((SearchRequest)searchRequest, OperationEntity.class, (ObjectMapper)this.objectMapper, (RestHighLevelClient)this.esClient);
            return DtoCreator.create((List)operationEntities, OperationDto.class);
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while searching for operation.", e.getMessage());
            logger.error(message, e);
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }
}
