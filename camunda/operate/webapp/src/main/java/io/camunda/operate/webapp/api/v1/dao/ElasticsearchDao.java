/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  io.camunda.operate.property.OperateProperties
 *  io.camunda.operate.util.ConversionUtils
 *  io.camunda.operate.webapp.api.v1.dao.PageableDao
 *  io.camunda.operate.webapp.api.v1.dao.SortableDao
 *  io.camunda.operate.webapp.api.v1.entities.Query
 *  io.camunda.operate.webapp.api.v1.entities.Query$Sort$Order
 *  org.elasticsearch.client.RestHighLevelClient
 *  org.elasticsearch.index.query.Operator
 *  org.elasticsearch.index.query.QueryBuilder
 *  org.elasticsearch.index.query.QueryBuilders
 *  org.elasticsearch.search.builder.SearchSourceBuilder
 *  org.elasticsearch.search.sort.FieldSortBuilder
 *  org.elasticsearch.search.sort.SortBuilders
 *  org.elasticsearch.search.sort.SortOrder
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.beans.factory.annotation.Qualifier
 */
package io.camunda.operate.webapp.api.v1.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.util.ConversionUtils;
import io.camunda.operate.webapp.api.v1.dao.PageableDao;
import io.camunda.operate.webapp.api.v1.dao.SortableDao;
import io.camunda.operate.webapp.api.v1.entities.Query;
import java.util.List;
import java.util.stream.Collectors;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public abstract class ElasticsearchDao<T>
implements SortableDao<T>,
PageableDao<T> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    @Qualifier(value="esClient")
    protected RestHighLevelClient elasticsearch;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected OperateProperties operateProperties;

    public void buildSorting(Query<T> query, String uniqueSortKey, SearchSourceBuilder searchSourceBuilder) {
        List<Query.Sort> sorts = query.getSort();
        if (sorts != null) {
            searchSourceBuilder.sort(sorts.stream().map(sort -> {
                Query.Sort.Order order = sort.getOrder();
                FieldSortBuilder sortBuilder = SortBuilders.fieldSort((String)sort.getField());
                if (!order.equals((Object)Query.Sort.Order.DESC)) return (FieldSortBuilder)sortBuilder.order(SortOrder.ASC);
                return (FieldSortBuilder)sortBuilder.order(SortOrder.DESC);
            }).collect(Collectors.toList()));
        }
        searchSourceBuilder.sort(SortBuilders.fieldSort((String)uniqueSortKey).order(SortOrder.ASC));
    }

    public void buildPaging(Query<T> query, SearchSourceBuilder searchSourceBuilder) {
        Object[] searchAfter = query.getSearchAfter();
        if (searchAfter != null) {
            searchSourceBuilder.searchAfter(searchAfter);
        }
        searchSourceBuilder.size(query.getSize());
    }

    protected SearchSourceBuilder buildQueryOn(Query<T> query, String uniqueKey, SearchSourceBuilder searchSourceBuilder) {
        this.logger.debug("Build query for Elasticsearch from query {}", (Object)query);
        this.buildSorting(query, uniqueKey, searchSourceBuilder);
        this.buildPaging(query, searchSourceBuilder);
        this.buildFiltering(query, searchSourceBuilder);
        return searchSourceBuilder;
    }

    protected abstract void buildFiltering(Query<T> var1, SearchSourceBuilder var2);

    protected QueryBuilder buildTermQuery(String name, String value) {
        if (ConversionUtils.stringIsEmpty((String)value)) return null;
        return QueryBuilders.termQuery((String)name, (String)value);
    }

    protected QueryBuilder buildTermQuery(String name, Integer value) {
        if (value == null) return null;
        return QueryBuilders.termQuery((String)name, (Object)value);
    }

    protected QueryBuilder buildTermQuery(String name, Long value) {
        if (value == null) return null;
        return QueryBuilders.termQuery((String)name, (Object)value);
    }

    protected QueryBuilder buildTermQuery(String name, Boolean value) {
        if (value == null) return null;
        return QueryBuilders.termQuery((String)name, (Object)value);
    }

    protected QueryBuilder buildMatchQuery(String name, String value) {
        if (value == null) return null;
        return QueryBuilders.matchQuery((String)name, (Object)value).operator(Operator.AND);
    }

    protected QueryBuilder buildMatchDateQuery(String name, String dateAsString) {
        if (dateAsString == null) return null;
        return QueryBuilders.rangeQuery((String)name).gte((Object)dateAsString).lte((Object)dateAsString).format(this.operateProperties.getElasticsearch().getDateFormat());
    }
}
