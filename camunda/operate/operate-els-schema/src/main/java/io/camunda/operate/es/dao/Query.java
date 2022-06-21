package io.camunda.operate.es.dao;

import io.camunda.operate.util.ElasticsearchUtil;
import java.util.Objects;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;

public class Query {
   private QueryBuilder queryBuilder = null;
   private AggregationBuilder aggregationBuilder = null;
   private String groupName = null;

   public static Query whereEquals(String field, String value) {
      Query instance = new Query();
      instance.queryBuilder = QueryBuilders.termsQuery(field, new String[]{value});
      return instance;
   }

   public static Query range(String field, Object gte, Object lte) {
      Query instance = new Query();
      RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(field);
      if (gte != null) {
         rangeQueryBuilder = rangeQueryBuilder.gte(gte);
      }

      if (lte != null) {
         rangeQueryBuilder = rangeQueryBuilder.lte(lte);
      }

      instance.queryBuilder = rangeQueryBuilder;
      return instance;
   }

   public Query aggregate(String groupName, String fieldName, int limit) {
      TermsAggregationBuilder aggregation = AggregationBuilders.terms(groupName);
      aggregation.field(fieldName);
      aggregation.size(limit);
      this.aggregationBuilder = aggregation;
      this.groupName = groupName;
      return this;
   }

   public Query aggregate(String groupName, String fieldName) {
      return this.aggregate(groupName, fieldName, Integer.MAX_VALUE);
   }

   public Query and(Query andQuery) {
      this.queryBuilder = ElasticsearchUtil.joinWithAnd(this.queryBuilder, andQuery.queryBuilder);
      return this;
   }

   QueryBuilder getQueryBuilder() {
      return this.queryBuilder;
   }

   AggregationBuilder getAggregationBuilder() {
      return this.aggregationBuilder;
   }

   String getGroupName() {
      return this.groupName;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof Query)) {
         return false;
      } else {
         Query query = (Query)o;
         return Objects.equals(this.queryBuilder, query.queryBuilder) && Objects.equals(this.aggregationBuilder, query.aggregationBuilder) && Objects.equals(this.groupName, query.groupName);
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.queryBuilder, this.aggregationBuilder, this.groupName});
   }

   public String toString() {
      return "Query{queryBuilder=" + this.queryBuilder + ", aggregationBuilder=" + this.aggregationBuilder + ", groupName='" + this.groupName + "'}";
   }
}
