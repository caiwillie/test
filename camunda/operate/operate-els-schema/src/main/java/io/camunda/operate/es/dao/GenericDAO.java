package io.camunda.operate.es.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.entities.OperateEntity;
import io.camunda.operate.es.dao.response.AggregationResponse;
import io.camunda.operate.es.dao.response.InsertResponse;
import io.camunda.operate.es.dao.response.SearchResponse;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.schema.indices.IndexDescriptor;
import io.camunda.operate.util.ElasticsearchUtil;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.stream.Collectors;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericDAO {
   private static final Logger LOGGER = LoggerFactory.getLogger(GenericDAO.class);
   private RestHighLevelClient esClient;
   private ObjectMapper objectMapper;
   private IndexDescriptor index;
   private Class typeOfEntity;

   private GenericDAO() {
   }

   GenericDAO(ObjectMapper objectMapper, IndexDescriptor index, RestHighLevelClient esClient) {
      if (objectMapper == null) {
         throw new IllegalStateException("ObjectMapper can't be null");
      } else if (index == null) {
         throw new IllegalStateException("Index can't be null");
      } else if (esClient == null) {
         throw new IllegalStateException("ES Client can't be null");
      } else {
         this.objectMapper = objectMapper;
         this.index = index;
         this.esClient = esClient;
         this.typeOfEntity = (Class)((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
      }
   }

   public IndexRequest buildESIndexRequest(OperateEntity entity) {
      try {
         return (new IndexRequest(this.index.getFullQualifiedName())).id(entity.getId()).source(this.objectMapper.writeValueAsString(entity), XContentType.JSON);
      } catch (JsonProcessingException var3) {
         throw new OperateRuntimeException("error building Index/InserRequest");
      }
   }

   public InsertResponse insert(OperateEntity entity) {
      try {
         IndexRequest request = this.buildESIndexRequest(entity);
         IndexResponse response = this.esClient.index(request, RequestOptions.DEFAULT);
         return response.status() != RestStatus.CREATED ? InsertResponse.failure() : InsertResponse.success();
      } catch (IOException var4) {
         LOGGER.error(var4.getMessage(), var4);
         throw new OperateRuntimeException("Error while trying to upsert entity: " + entity);
      }
   }

   public SearchResponse search(Query query) {
      SearchSourceBuilder source = SearchSourceBuilder.searchSource().query(query.getQueryBuilder()).aggregation(query.getAggregationBuilder());
      SearchRequest searchRequest = (new SearchRequest(new String[]{this.index.getFullQualifiedName()})).indicesOptions(IndicesOptions.lenientExpandOpen()).source(source);

      try {
         List hits = ElasticsearchUtil.scroll(searchRequest, this.typeOfEntity, this.objectMapper, this.esClient);
         return new SearchResponse(false, hits);
      } catch (IOException var5) {
         LOGGER.error("Error searching at index: " + this.index, var5);
         return new SearchResponse(true);
      }
   }

   public AggregationResponse searchWithAggregation(Query query) {
      SearchSourceBuilder source = SearchSourceBuilder.searchSource().query(query.getQueryBuilder()).aggregation(query.getAggregationBuilder());
      SearchRequest searchRequest = (new SearchRequest(new String[]{this.index.getFullQualifiedName()})).indicesOptions(IndicesOptions.lenientExpandOpen()).source(source);

      try {
         Aggregations aggregations = this.esClient.search(searchRequest, RequestOptions.DEFAULT).getAggregations();
         if (aggregations == null) {
            throw new OperateRuntimeException("Search with aggregation returned no aggregation");
         } else {
            Aggregation group = aggregations.get(query.getGroupName());
            if (!(group instanceof ParsedStringTerms)) {
               throw new OperateRuntimeException("Unexpected response for aggregations");
            } else {
               ParsedStringTerms terms = (ParsedStringTerms)group;
               List buckets = terms.getBuckets();
               List values = (List)buckets.stream().map((it) -> {
                  return new AggregationResponse.AggregationValue(String.valueOf(it.getKey()), it.getDocCount());
               }).collect(Collectors.toList());
               long sumOfOtherDocCounts = ((ParsedStringTerms)group).getSumOfOtherDocCounts();
               long total = sumOfOtherDocCounts + (long)values.size();
               return new AggregationResponse(false, values, total);
            }
         }
      } catch (IOException var13) {
         LOGGER.error("Error searching at index: " + this.index, var13);
         return new AggregationResponse(true);
      }
   }

   public static class Builder {
      private ObjectMapper objectMapper;
      private RestHighLevelClient esClient;
      private IndexDescriptor index;

      public Builder objectMapper(ObjectMapper objectMapper) {
         this.objectMapper = objectMapper;
         return this;
      }

      public Builder index(IndexDescriptor index) {
         this.index = index;
         return this;
      }

      public Builder esClient(RestHighLevelClient esClient) {
         this.esClient = esClient;
         return this;
      }

      public GenericDAO build() {
         if (this.objectMapper == null) {
            throw new IllegalStateException("ObjectMapper can't be null");
         } else if (this.index == null) {
            throw new IllegalStateException("Index can't be null");
         } else if (this.esClient == null) {
            throw new IllegalStateException("ES Client can't be null");
         } else {
            return new GenericDAO(this.objectMapper, this.index, this.esClient);
         }
      }
   }
}
