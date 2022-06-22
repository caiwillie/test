/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  io.camunda.operate.entities.dmn.DecisionInstanceEntity
 *  io.camunda.operate.entities.dmn.DecisionInstanceState
 *  io.camunda.operate.exceptions.OperateRuntimeException
 *  io.camunda.operate.property.OperateProperties
 *  io.camunda.operate.schema.indices.DecisionIndex
 *  io.camunda.operate.schema.templates.DecisionInstanceTemplate
 *  io.camunda.operate.schema.templates.TemplateDescriptor
 *  io.camunda.operate.util.CollectionUtil
 *  io.camunda.operate.util.ElasticsearchUtil
 *  io.camunda.operate.util.ElasticsearchUtil$QueryType
 *  io.camunda.operate.webapp.es.reader.AbstractReader
 *  io.camunda.operate.webapp.rest.dto.DtoCreator
 *  io.camunda.operate.webapp.rest.dto.dmn.DRDDataEntryDto
 *  io.camunda.operate.webapp.rest.dto.dmn.DecisionInstanceDto
 *  io.camunda.operate.webapp.rest.dto.dmn.list.DecisionInstanceForListDto
 *  io.camunda.operate.webapp.rest.dto.dmn.list.DecisionInstanceListQueryDto
 *  io.camunda.operate.webapp.rest.dto.dmn.list.DecisionInstanceListRequestDto
 *  io.camunda.operate.webapp.rest.dto.dmn.list.DecisionInstanceListResponseDto
 *  io.camunda.operate.webapp.rest.exception.NotFoundException
 *  org.elasticsearch.action.search.SearchRequest
 *  org.elasticsearch.action.search.SearchResponse
 *  org.elasticsearch.client.RequestOptions
 *  org.elasticsearch.client.RestHighLevelClient
 *  org.elasticsearch.index.query.QueryBuilder
 *  org.elasticsearch.index.query.QueryBuilders
 *  org.elasticsearch.index.query.RangeQueryBuilder
 *  org.elasticsearch.search.SearchHit
 *  org.elasticsearch.search.builder.SearchSourceBuilder
 *  org.elasticsearch.search.sort.FieldSortBuilder
 *  org.elasticsearch.search.sort.SortBuilder
 *  org.elasticsearch.search.sort.SortBuilders
 *  org.elasticsearch.search.sort.SortOrder
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.es.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.entities.dmn.DecisionInstanceEntity;
import io.camunda.operate.entities.dmn.DecisionInstanceState;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.schema.indices.DecisionIndex;
import io.camunda.operate.schema.templates.DecisionInstanceTemplate;
import io.camunda.operate.schema.templates.TemplateDescriptor;
import io.camunda.operate.util.CollectionUtil;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.webapp.es.reader.AbstractReader;
import io.camunda.operate.webapp.rest.dto.DtoCreator;
import io.camunda.operate.webapp.rest.dto.dmn.DRDDataEntryDto;
import io.camunda.operate.webapp.rest.dto.dmn.DecisionInstanceDto;
import io.camunda.operate.webapp.rest.dto.dmn.list.DecisionInstanceForListDto;
import io.camunda.operate.webapp.rest.dto.dmn.list.DecisionInstanceListQueryDto;
import io.camunda.operate.webapp.rest.dto.dmn.list.DecisionInstanceListRequestDto;
import io.camunda.operate.webapp.rest.dto.dmn.list.DecisionInstanceListResponseDto;
import io.camunda.operate.webapp.rest.exception.NotFoundException;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DecisionInstanceReader
extends AbstractReader {
    private static final Logger logger = LoggerFactory.getLogger(DecisionInstanceReader.class);
    @Autowired
    private DecisionInstanceTemplate decisionInstanceTemplate;
    @Autowired
    private DecisionIndex decisionIndex;
    @Autowired
    private DateTimeFormatter dateTimeFormatter;
    @Autowired
    private OperateProperties operateProperties;

    public DecisionInstanceDto getDecisionInstance(String decisionInstanceId) {
        QueryBuilder query = ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{QueryBuilders.idsQuery().addIds(new String[]{String.valueOf(decisionInstanceId)}), QueryBuilders.termQuery((String)"id", (String)decisionInstanceId)});
        SearchRequest request = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.decisionInstanceTemplate, (ElasticsearchUtil.QueryType)ElasticsearchUtil.QueryType.ALL).source(new SearchSourceBuilder().query((QueryBuilder)QueryBuilders.constantScoreQuery((QueryBuilder)query)));
        try {
            SearchResponse response = this.esClient.search(request, RequestOptions.DEFAULT);
            if (response.getHits().getTotalHits().value == 1L) {
                DecisionInstanceEntity decisionInstance = (DecisionInstanceEntity)ElasticsearchUtil.fromSearchHit((String)response.getHits().getHits()[0].getSourceAsString(), (ObjectMapper)this.objectMapper, DecisionInstanceEntity.class);
                return (DecisionInstanceDto)DtoCreator.create(decisionInstance, DecisionInstanceDto.class);
            }
            if (response.getHits().getTotalHits().value <= 1L) throw new NotFoundException(String.format("Could not find decision instance with id '%s'.", decisionInstanceId));
            throw new NotFoundException(String.format("Could not find unique decision instance with id '%s'.", decisionInstanceId));
        }
        catch (IOException ex) {
            throw new OperateRuntimeException(ex.getMessage(), (Throwable)ex);
        }
    }

    public DecisionInstanceListResponseDto queryDecisionInstances(DecisionInstanceListRequestDto request) {
        DecisionInstanceListResponseDto result = new DecisionInstanceListResponseDto();
        List<DecisionInstanceEntity> entities = this.queryDecisionInstancesEntities(request, result);
        result.setDecisionInstances(DtoCreator.create(entities, DecisionInstanceForListDto.class));
        return result;
    }

    private List<DecisionInstanceEntity> queryDecisionInstancesEntities(DecisionInstanceListRequestDto request, DecisionInstanceListResponseDto result) {
        QueryBuilder query = this.createRequestQuery(request.getQuery());
        logger.debug("Decision instance search request: \n{}", (Object)query.toString());
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(query).fetchSource(null, new String[]{"result", "evaluatedInputs", "evaluatedOutputs"});
        this.applySorting(searchSourceBuilder, request);
        SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.decisionInstanceTemplate).source(searchSourceBuilder);
        logger.debug("Search request will search in: \n{}", searchRequest.indices());
        try {
            SearchResponse response = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
            result.setTotalCount(response.getHits().getTotalHits().value);
            List decisionInstanceEntities = ElasticsearchUtil.mapSearchHits((SearchHit[])response.getHits().getHits(), sh -> {
                DecisionInstanceEntity entity = (DecisionInstanceEntity)ElasticsearchUtil.fromSearchHit((String)sh.getSourceAsString(), (ObjectMapper)this.objectMapper, DecisionInstanceEntity.class);
                entity.setSortValues(sh.getSortValues());
                return entity;
            });
            if (request.getSearchBefore() == null) return decisionInstanceEntities;
            Collections.reverse(decisionInstanceEntities);
            return decisionInstanceEntities;
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining instances list: %s", e.getMessage());
            logger.error(message, e);
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    private void applySorting(SearchSourceBuilder searchSourceBuilder, DecisionInstanceListRequestDto request) {
        Object[] querySearchAfter;
        SortBuilder sort3;
        SortBuilder sort2;
        boolean directSorting;
        String sortBy = this.getSortBy(request);
        boolean bl = directSorting = request.getSearchAfter() != null || request.getSearchBefore() == null;
        if (request.getSorting() != null) {
            SortOrder sort1DirectOrder = SortOrder.fromString((String)request.getSorting().getSortOrder());
            FieldSortBuilder sort1 = directSorting ? ((FieldSortBuilder)SortBuilders.fieldSort((String)sortBy).order(sort1DirectOrder)).missing((Object)"_last") : ((FieldSortBuilder)SortBuilders.fieldSort((String)sortBy).order(this.reverseOrder(sort1DirectOrder))).missing((Object)"_first");
            searchSourceBuilder.sort((SortBuilder)sort1);
        }
        if (directSorting) {
            sort2 = SortBuilders.fieldSort((String)"key").order(SortOrder.ASC);
            sort3 = SortBuilders.fieldSort((String)"executionIndex").order(SortOrder.ASC);
            querySearchAfter = request.getSearchAfter();
        } else {
            sort2 = SortBuilders.fieldSort((String)"key").order(SortOrder.DESC);
            sort3 = SortBuilders.fieldSort((String)"executionIndex").order(SortOrder.DESC);
            querySearchAfter = request.getSearchBefore();
        }
        searchSourceBuilder.sort(sort2).sort(sort3).size(request.getPageSize().intValue());
        if (querySearchAfter == null) return;
        searchSourceBuilder.searchAfter(querySearchAfter);
    }

    private String getSortBy(DecisionInstanceListRequestDto request) {
        if (request.getSorting() == null) return null;
        String sortBy = request.getSorting().getSortBy();
        if (sortBy.equals("id")) {
            sortBy = "key";
        } else {
            if (!sortBy.equals("processInstanceId")) return sortBy;
            sortBy = "processInstanceKey";
        }
        return sortBy;
    }

    private SortOrder reverseOrder(SortOrder sortOrder) {
        if (!sortOrder.equals((Object)SortOrder.ASC)) return SortOrder.ASC;
        return SortOrder.DESC;
    }

    private QueryBuilder createRequestQuery(DecisionInstanceListQueryDto query) {
        QueryBuilder queryBuilder = ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{this.createEvaluatedFailedQuery(query), this.createDecisionDefinitionIdsQuery(query), this.createIdsQuery(query), this.createProcessInstanceIdQuery(query), this.createEvaluationDateQuery(query)});
        if (queryBuilder != null) return queryBuilder;
        queryBuilder = QueryBuilders.matchAllQuery();
        return queryBuilder;
    }

    private QueryBuilder createEvaluationDateQuery(DecisionInstanceListQueryDto query) {
        if (query.getEvaluationDateAfter() == null) {
            if (query.getEvaluationDateBefore() == null) return null;
        }
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery((String)"evaluationDate");
        if (query.getEvaluationDateAfter() != null) {
            rangeQueryBuilder.gte((Object)this.dateTimeFormatter.format(query.getEvaluationDateAfter()));
        }
        if (query.getEvaluationDateBefore() != null) {
            rangeQueryBuilder.lt((Object)this.dateTimeFormatter.format(query.getEvaluationDateBefore()));
        }
        rangeQueryBuilder.format(this.operateProperties.getElasticsearch().getElsDateFormat());
        return rangeQueryBuilder;
    }

    private QueryBuilder createProcessInstanceIdQuery(DecisionInstanceListQueryDto query) {
        if (query.getProcessInstanceId() == null) return null;
        return QueryBuilders.termQuery((String)"processInstanceKey", (String)query.getProcessInstanceId());
    }

    private QueryBuilder createIdsQuery(DecisionInstanceListQueryDto query) {
        if (!CollectionUtil.isNotEmpty((Collection)query.getIds())) return null;
        return QueryBuilders.termsQuery((String)"id", (Collection)query.getIds());
    }

    private QueryBuilder createDecisionDefinitionIdsQuery(DecisionInstanceListQueryDto query) {
        if (!CollectionUtil.isNotEmpty((Collection)query.getDecisionDefinitionIds())) return null;
        return QueryBuilders.termsQuery((String)"decisionDefinitionId", (Collection)query.getDecisionDefinitionIds());
    }

    private QueryBuilder createEvaluatedFailedQuery(DecisionInstanceListQueryDto query) {
        if (query.isEvaluated() && query.isFailed()) {
            return null;
        }
        if (query.isFailed()) {
            return QueryBuilders.termQuery((String)"state", (Object)DecisionInstanceState.FAILED);
        }
        if (!query.isEvaluated()) return ElasticsearchUtil.createMatchNoneQuery();
        return QueryBuilders.termQuery((String)"state", (Object)DecisionInstanceState.EVALUATED);
    }

    public Map<String, List<DRDDataEntryDto>> getDecisionInstanceDRDData(String decisionInstanceId) {
        Long decisionInstanceKey = DecisionInstanceEntity.extractKey((String)decisionInstanceId);
        SearchRequest request = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.decisionInstanceTemplate).source(new SearchSourceBuilder().query((QueryBuilder)QueryBuilders.termQuery((String)"key", (Object)decisionInstanceKey)).fetchSource(new String[]{"decisionId", "state"}, null).sort("evaluationDate", SortOrder.ASC));
        try {
            List<DRDDataEntryDto> entries = ElasticsearchUtil.scroll((SearchRequest)request, DRDDataEntryDto.class, (ObjectMapper)this.objectMapper, (RestHighLevelClient)this.esClient, sh -> {
                Map map = sh.getSourceAsMap();
                return new DRDDataEntryDto(sh.getId(), (String)map.get("decisionId"), DecisionInstanceState.valueOf((String)((String)map.get("state"))));
            }, null, null);
            return entries.stream().collect(Collectors.groupingBy(DRDDataEntryDto::getDecisionId));
        }
        catch (IOException e) {
            throw new OperateRuntimeException("Exception occurred while quiering DRD data for decision instance id: " + decisionInstanceId);
        }
    }
}
