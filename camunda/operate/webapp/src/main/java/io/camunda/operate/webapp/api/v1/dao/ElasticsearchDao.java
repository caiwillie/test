package io.camunda.operate.webapp.api.v1.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.util.ConversionUtils;
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

public abstract class ElasticsearchDao implements SortableDao, PageableDao {
   protected final Logger logger = LoggerFactory.getLogger(this.getClass());
   @Autowired
   @Qualifier("esClient")
   protected RestHighLevelClient elasticsearch;
   @Autowired
   protected ObjectMapper objectMapper;
   @Autowired
   protected OperateProperties operateProperties;

   public void buildSorting(Query query, String uniqueSortKey, SearchSourceBuilder searchSourceBuilder) {
      List<Query.Sort> sorts = query.getSort();
      if (sorts != null) {
         searchSourceBuilder.sort((List)sorts.stream().map((sort) -> {
            Query.Sort.Order order = sort.getOrder();
            FieldSortBuilder sortBuilder = SortBuilders.fieldSort(sort.getField());
            return order.equals(Query.Sort.Order.DESC) ? (FieldSortBuilder)sortBuilder.order(SortOrder.DESC) : (FieldSortBuilder)sortBuilder.order(SortOrder.ASC);
         }).collect(Collectors.toList()));
      }

      searchSourceBuilder.sort(SortBuilders.fieldSort(uniqueSortKey).order(SortOrder.ASC));
   }

   public void buildPaging(Query query, SearchSourceBuilder searchSourceBuilder) {
      Object[] searchAfter = query.getSearchAfter();
      if (searchAfter != null) {
         searchSourceBuilder.searchAfter(searchAfter);
      }

      searchSourceBuilder.size(query.getSize());
   }

   protected SearchSourceBuilder buildQueryOn(Query query, String uniqueKey, SearchSourceBuilder searchSourceBuilder) {
      this.logger.debug("Build query for Elasticsearch from query {}", query);
      this.buildSorting(query, uniqueKey, searchSourceBuilder);
      this.buildPaging(query, searchSourceBuilder);
      this.buildFiltering(query, searchSourceBuilder);
      return searchSourceBuilder;
   }

   protected abstract void buildFiltering(Query var1, SearchSourceBuilder var2);

   protected QueryBuilder buildTermQuery(String name, String value) {
      return !ConversionUtils.stringIsEmpty(value) ? QueryBuilders.termQuery(name, value) : null;
   }

   protected QueryBuilder buildTermQuery(String name, Integer value) {
      return value != null ? QueryBuilders.termQuery(name, value) : null;
   }

   protected QueryBuilder buildTermQuery(String name, Long value) {
      return value != null ? QueryBuilders.termQuery(name, value) : null;
   }

   protected QueryBuilder buildTermQuery(String name, Boolean value) {
      return value != null ? QueryBuilders.termQuery(name, value) : null;
   }

   protected QueryBuilder buildMatchQuery(String name, String value) {
      return value != null ? QueryBuilders.matchQuery(name, value).operator(Operator.AND) : null;
   }

   protected QueryBuilder buildMatchDateQuery(String name, String dateAsString) {
      return dateAsString != null ? QueryBuilders.rangeQuery(name).gte(dateAsString).lte(dateAsString).format(this.operateProperties.getElasticsearch().getDateFormat()) : null;
   }
}
