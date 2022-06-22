/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  io.camunda.operate.entities.VariableEntity
 *  io.camunda.operate.exceptions.OperateRuntimeException
 *  io.camunda.operate.property.OperateProperties
 *  io.camunda.operate.schema.templates.TemplateDescriptor
 *  io.camunda.operate.schema.templates.VariableTemplate
 *  io.camunda.operate.util.ElasticsearchUtil
 *  io.camunda.operate.util.ElasticsearchUtil$QueryType
 *  io.camunda.operate.webapp.es.reader.AbstractReader
 *  io.camunda.operate.webapp.es.reader.OperationReader
 *  io.camunda.operate.webapp.rest.dto.VariableDto
 *  io.camunda.operate.webapp.rest.dto.VariableRequestDto
 *  io.camunda.operate.webapp.rest.exception.NotFoundException
 *  org.elasticsearch.action.search.SearchRequest
 *  org.elasticsearch.action.search.SearchResponse
 *  org.elasticsearch.client.RequestOptions
 *  org.elasticsearch.index.query.ConstantScoreQueryBuilder
 *  org.elasticsearch.index.query.IdsQueryBuilder
 *  org.elasticsearch.index.query.QueryBuilder
 *  org.elasticsearch.index.query.QueryBuilders
 *  org.elasticsearch.index.query.TermQueryBuilder
 *  org.elasticsearch.search.SearchHit
 *  org.elasticsearch.search.builder.SearchSourceBuilder
 *  org.elasticsearch.search.sort.SortOrder
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.es.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.entities.VariableEntity;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.schema.templates.TemplateDescriptor;
import io.camunda.operate.schema.templates.VariableTemplate;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.webapp.es.reader.AbstractReader;
import io.camunda.operate.webapp.es.reader.OperationReader;
import io.camunda.operate.webapp.rest.dto.VariableDto;
import io.camunda.operate.webapp.rest.dto.VariableRequestDto;
import io.camunda.operate.webapp.rest.exception.NotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.ConstantScoreQueryBuilder;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VariableReader
extends AbstractReader {
    private static final Logger logger = LoggerFactory.getLogger(VariableReader.class);
    @Autowired
    private VariableTemplate variableTemplate;
    @Autowired
    private OperationReader operationReader;
    @Autowired
    private OperateProperties operateProperties;

    public List<VariableDto> getVariables(String processInstanceId, VariableRequestDto request) {
        List<VariableDto> response = this.queryVariables(processInstanceId, request);
        if (request.getSearchAfterOrEqual() != null || request.getSearchBeforeOrEqual() != null) {
            this.adjustResponse(response, processInstanceId, request);
        }
        if (response.size() <= 0) return response;
        if (request.getSearchAfter() == null) {
            if (request.getSearchAfterOrEqual() == null) return response;
        }
        VariableDto firstVar = response.get(0);
        firstVar.setIsFirst(this.checkVarIsFirst(processInstanceId, request, firstVar.getId()));
        return response;
    }

    private boolean checkVarIsFirst(String processInstanceId, VariableRequestDto query, String id) {
        VariableRequestDto newQuery = (VariableRequestDto)((VariableRequestDto)((VariableRequestDto)((VariableRequestDto)((VariableRequestDto)query.createCopy().setSearchAfter(null)).setSearchAfterOrEqual(null)).setSearchBefore(null)).setSearchBeforeOrEqual(null)).setPageSize(Integer.valueOf(1));
        List<VariableDto> vars = this.queryVariables(processInstanceId, newQuery);
        if (vars.size() <= 0) return false;
        return vars.get(0).getId().equals(id);
    }

    private void adjustResponse(List<VariableDto> response, String processInstanceId, VariableRequestDto request) {
        String variableName = null;
        if (request.getSearchAfterOrEqual() != null) {
            variableName = (String)request.getSearchAfterOrEqual()[0];
        } else if (request.getSearchBeforeOrEqual() != null) {
            variableName = (String)request.getSearchBeforeOrEqual()[0];
        }
        VariableRequestDto newRequest = (VariableRequestDto)((VariableRequestDto)((VariableRequestDto)((VariableRequestDto)request.createCopy().setSearchAfter(null)).setSearchAfterOrEqual(null)).setSearchBefore(null)).setSearchBeforeOrEqual(null);
        List<VariableDto> entities = this.queryVariables(processInstanceId, newRequest, variableName);
        if (entities.size() <= 0) return;
        VariableDto entity = entities.get(0);
        entity.setIsFirst(false);
        if (request.getSearchAfterOrEqual() != null) {
            if (request.getPageSize() != null && response.size() == request.getPageSize().intValue()) {
                response.remove(response.size() - 1);
            }
            response.add(0, entity);
        } else {
            if (request.getSearchBeforeOrEqual() == null) return;
            if (request.getPageSize() != null && response.size() == request.getPageSize().intValue()) {
                response.remove(0);
            }
            response.add(entity);
        }
    }

    private List<VariableDto> queryVariables(String processInstanceId, VariableRequestDto variableRequest) {
        return this.queryVariables(processInstanceId, variableRequest, null);
    }

    private List<VariableDto> queryVariables(String processInstanceId, VariableRequestDto request, String varName) {
        Long scopeKey = null;
        if (request.getScopeId() != null) {
            scopeKey = Long.valueOf(request.getScopeId());
        }
        TermQueryBuilder processInstanceKeyQuery = QueryBuilders.termQuery((String)"processInstanceKey", (String)processInstanceId);
        TermQueryBuilder scopeKeyQuery = QueryBuilders.termQuery((String)"scopeKey", (Object)scopeKey);
        TermQueryBuilder varNameQ = null;
        if (varName != null) {
            varNameQ = QueryBuilders.termQuery((String)"name", (String)varName);
        }
        ConstantScoreQueryBuilder query = QueryBuilders.constantScoreQuery((QueryBuilder)ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{processInstanceKeyQuery, scopeKeyQuery, varNameQ}));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query((QueryBuilder)query).fetchSource(null, "fullValue");
        this.applySorting(searchSourceBuilder, request);
        SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.variableTemplate, (ElasticsearchUtil.QueryType)ElasticsearchUtil.QueryType.ALL).source(searchSourceBuilder);
        try {
            SearchResponse response = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
            List variableEntities = ElasticsearchUtil.mapSearchHits((SearchHit[])response.getHits().getHits(), sh -> {
                VariableEntity entity = (VariableEntity)ElasticsearchUtil.fromSearchHit((String)sh.getSourceAsString(), (ObjectMapper)this.objectMapper, VariableEntity.class);
                entity.setSortValues(sh.getSortValues());
                return entity;
            });
            Map operations = this.operationReader.getUpdateOperationsPerVariableName(Long.valueOf(processInstanceId), scopeKey);
            List variables = VariableDto.createFrom((List)variableEntities, (Map)operations, (int)this.operateProperties.getImporter().getVariableSizeThreshold());
            if (variables.size() <= 0) return variables;
            if (request.getSearchBefore() != null || request.getSearchBeforeOrEqual() != null) {
                if (variables.size() <= request.getPageSize()) {
                    ((VariableDto)variables.get(variables.size() - 1)).setIsFirst(true);
                } else {
                    variables.remove(variables.size() - 1);
                }
                Collections.reverse(variables);
            } else {
                if (request.getSearchAfter() != null) return variables;
                if (request.getSearchAfterOrEqual() != null) return variables;
                ((VariableDto)variables.get(0)).setIsFirst(true);
            }
            return variables;
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining variables: %s", e.getMessage());
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    private void applySorting(SearchSourceBuilder searchSourceBuilder, VariableRequestDto request) {
        boolean directSorting;
        boolean bl = directSorting = request.getSearchAfter() != null || request.getSearchAfterOrEqual() != null || request.getSearchBefore() == null && request.getSearchBeforeOrEqual() == null;
        if (directSorting) {
            searchSourceBuilder.sort("name", SortOrder.ASC);
            if (request.getSearchAfter() != null) {
                searchSourceBuilder.searchAfter(request.getSearchAfter());
            } else if (request.getSearchAfterOrEqual() != null) {
                searchSourceBuilder.searchAfter(request.getSearchAfterOrEqual());
            }
            searchSourceBuilder.size(request.getPageSize().intValue());
        } else {
            searchSourceBuilder.sort("name", SortOrder.DESC);
            if (request.getSearchBefore() != null) {
                searchSourceBuilder.searchAfter(request.getSearchBefore());
            } else if (request.getSearchBeforeOrEqual() != null) {
                searchSourceBuilder.searchAfter(request.getSearchBeforeOrEqual());
            }
            searchSourceBuilder.size(request.getPageSize() + 1);
        }
    }

    public VariableDto getVariable(String id) {
        IdsQueryBuilder idsQ = QueryBuilders.idsQuery().addIds(new String[]{id});
        SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.variableTemplate, (ElasticsearchUtil.QueryType)ElasticsearchUtil.QueryType.ALL).source(new SearchSourceBuilder().query((QueryBuilder)idsQ));
        try {
            SearchResponse response = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
            if (response.getHits().getTotalHits().value != 1L) {
                throw new NotFoundException(String.format("Variable with id %s not found.", id));
            }
            VariableEntity variableEntity = (VariableEntity)ElasticsearchUtil.fromSearchHit((String)response.getHits().getHits()[0].getSourceAsString(), (ObjectMapper)this.objectMapper, VariableEntity.class);
            return VariableDto.createFrom((VariableEntity)variableEntity, null, (boolean)true, (int)this.operateProperties.getImporter().getVariableSizeThreshold());
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining variable: %s", e.getMessage());
            logger.error(message, e);
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    public VariableDto getVariableByName(String processInstanceId, String scopeId, String variableName) {
        TermQueryBuilder processInstanceIdQ = QueryBuilders.termQuery((String)"processInstanceKey", (String)processInstanceId);
        TermQueryBuilder scopeIdQ = QueryBuilders.termQuery((String)"scopeKey", (String)scopeId);
        TermQueryBuilder varNameQ = QueryBuilders.termQuery((String)"name", (String)variableName);
        SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.variableTemplate, (ElasticsearchUtil.QueryType)ElasticsearchUtil.QueryType.ALL).source(new SearchSourceBuilder().query((QueryBuilder)QueryBuilders.constantScoreQuery((QueryBuilder)ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{processInstanceIdQ, scopeIdQ, varNameQ}))));
        try {
            SearchResponse response = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
            if (response.getHits().getTotalHits().value <= 0L) return null;
            VariableEntity variableEntity = (VariableEntity)ElasticsearchUtil.fromSearchHit((String)response.getHits().getHits()[0].getSourceAsString(), (ObjectMapper)this.objectMapper, VariableEntity.class);
            return VariableDto.createFrom((VariableEntity)variableEntity, null, (boolean)true, (int)this.operateProperties.getImporter().getVariableSizeThreshold());
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining variable for processInstanceId: %s, scopeId: %s, name: %s, error: %s", processInstanceId, scopeId, variableName, e.getMessage());
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }
}
