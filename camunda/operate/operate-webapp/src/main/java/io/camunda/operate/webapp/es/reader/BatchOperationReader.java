/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  io.camunda.operate.entities.BatchOperationEntity
 *  io.camunda.operate.exceptions.OperateRuntimeException
 *  io.camunda.operate.schema.templates.BatchOperationTemplate
 *  io.camunda.operate.util.ElasticsearchUtil
 *  io.camunda.operate.webapp.rest.dto.operation.BatchOperationRequestDto
 *  io.camunda.operate.webapp.security.UserService
 *  org.elasticsearch.action.search.SearchRequest
 *  org.elasticsearch.action.search.SearchResponse
 *  org.elasticsearch.client.RequestOptions
 *  org.elasticsearch.client.Requests
 *  org.elasticsearch.client.RestHighLevelClient
 *  org.elasticsearch.index.query.QueryBuilder
 *  org.elasticsearch.index.query.QueryBuilders
 *  org.elasticsearch.index.query.TermQueryBuilder
 *  org.elasticsearch.search.SearchHit
 *  org.elasticsearch.search.builder.SearchSourceBuilder
 *  org.elasticsearch.search.sort.FieldSortBuilder
 *  org.elasticsearch.search.sort.SortBuilder
 *  org.elasticsearch.search.sort.SortOrder
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.es.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.entities.BatchOperationEntity;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.schema.templates.BatchOperationTemplate;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.webapp.rest.dto.operation.BatchOperationRequestDto;
import io.camunda.operate.webapp.security.UserService;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BatchOperationReader {
    private static final Logger logger = LoggerFactory.getLogger(BatchOperationReader.class);
    @Autowired
    private BatchOperationTemplate batchOperationTemplate;
    @Autowired
    private UserService userService;
    @Autowired
    private RestHighLevelClient esClient;
    @Autowired
    private ObjectMapper objectMapper;

    public List<BatchOperationEntity> getBatchOperations(BatchOperationRequestDto batchOperationRequestDto) {
        SearchRequest searchRequest = this.createSearchRequest(batchOperationRequestDto);
        try {
            SearchResponse searchResponse = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
            List batchOperationEntities = ElasticsearchUtil.mapSearchHits((SearchHit[])searchResponse.getHits().getHits(), sh -> {
                BatchOperationEntity entity = (BatchOperationEntity)ElasticsearchUtil.fromSearchHit((String)sh.getSourceAsString(), (ObjectMapper)this.objectMapper, BatchOperationEntity.class);
                entity.setSortValues(sh.getSortValues());
                return entity;
            });
            if (batchOperationRequestDto.getSearchBefore() == null) return batchOperationEntities;
            Collections.reverse(batchOperationEntities);
            return batchOperationEntities;
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while getting page of batch operations list: %s", e.getMessage());
            logger.error(message, e);
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    private SearchRequest createSearchRequest(BatchOperationRequestDto batchOperationRequestDto) {
        Object[] querySearchAfter;
        SortBuilder sort2;
        FieldSortBuilder sort1;
        TermQueryBuilder queryBuilder = QueryBuilders.termQuery((String)"username", (String)this.userService.getCurrentUser().getUsername());
        Object[] searchAfter = batchOperationRequestDto.getSearchAfter();
        Object[] searchBefore = batchOperationRequestDto.getSearchBefore();
        if (searchAfter != null || searchBefore == null) {
            sort1 = ((FieldSortBuilder)new FieldSortBuilder("endDate").order(SortOrder.DESC)).missing((Object)"_first");
            sort2 = new FieldSortBuilder("startDate").order(SortOrder.DESC);
            querySearchAfter = searchAfter;
        } else {
            sort1 = ((FieldSortBuilder)new FieldSortBuilder("endDate").order(SortOrder.ASC)).missing((Object)"_last");
            sort2 = new FieldSortBuilder("startDate").order(SortOrder.ASC);
            querySearchAfter = searchBefore;
        }
        SearchSourceBuilder sourceBuilder = SearchSourceBuilder.searchSource().query((QueryBuilder)QueryBuilders.constantScoreQuery((QueryBuilder)queryBuilder)).sort((SortBuilder)sort1).sort(sort2).size(batchOperationRequestDto.getPageSize().intValue());
        if (querySearchAfter == null) return Requests.searchRequest((String[])new String[]{this.batchOperationTemplate.getAlias()}).source(sourceBuilder);
        sourceBuilder.searchAfter(querySearchAfter);
        return Requests.searchRequest((String[])new String[]{this.batchOperationTemplate.getAlias()}).source(sourceBuilder);
    }
}
