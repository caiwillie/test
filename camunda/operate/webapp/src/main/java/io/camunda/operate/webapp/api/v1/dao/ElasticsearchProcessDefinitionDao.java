package io.camunda.operate.webapp.api.v1.dao;

import io.camunda.operate.schema.indices.ProcessIndex;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.webapp.api.v1.entities.ProcessDefinition;
import io.camunda.operate.webapp.api.v1.entities.Query;
import io.camunda.operate.webapp.api.v1.entities.Results;
import io.camunda.operate.webapp.api.v1.exceptions.APIException;
import io.camunda.operate.webapp.api.v1.exceptions.ResourceNotFoundException;
import io.camunda.operate.webapp.api.v1.exceptions.ServerException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("ElasticsearchProcessDefinitionDaoV1")
public class ElasticsearchProcessDefinitionDao extends ElasticsearchDao implements ProcessDefinitionDao {
   @Autowired
   private ProcessIndex processIndex;

   public Results search(Query query) throws APIException {
      this.logger.debug("search {}", query);
      SearchSourceBuilder searchSourceBuilder = this.buildQueryOn(query, "key", new SearchSourceBuilder());

      try {
         SearchRequest searchRequest = (new SearchRequest()).indices(new String[]{this.processIndex.getAlias()}).source(searchSourceBuilder);
         SearchResponse searchResponse = this.elasticsearch.search(searchRequest, RequestOptions.DEFAULT);
         SearchHits searchHits = searchResponse.getHits();
         SearchHit[] searchHitArray = searchHits.getHits();
         if (searchHitArray != null && searchHitArray.length > 0) {
            Object[] sortValues = searchHitArray[searchHitArray.length - 1].getSortValues();
            return (new Results()).setTotal(searchHits.getTotalHits().value).setItems(ElasticsearchUtil.mapSearchHits(searchHitArray, this.objectMapper, ProcessDefinition.class)).setSortValues(sortValues);
         } else {
            return (new Results()).setTotal(searchHits.getTotalHits().value);
         }
      } catch (Exception var8) {
         throw new ServerException("Error in reading process definitions", var8);
      }
   }

   public ProcessDefinition byKey(Long key) throws APIException {
      this.logger.debug("byKey {}", key);

      List processDefinitions;
      try {
         processDefinitions = this.searchFor((new SearchSourceBuilder()).query(QueryBuilders.termQuery("key", key)));
      } catch (Exception var4) {
         throw new ServerException(String.format("Error in reading process definition for key %s", key), var4);
      }

      if (processDefinitions.isEmpty()) {
         throw new ResourceNotFoundException(String.format("No process definition found for key %s ", key));
      } else if (processDefinitions.size() > 1) {
         throw new ServerException(String.format("Found more than one process definition for key %s", key));
      } else {
         return (ProcessDefinition)processDefinitions.get(0);
      }
   }

   public String xmlByKey(Long key) throws APIException {
      try {
         SearchRequest searchRequest = (new SearchRequest(new String[]{this.processIndex.getAlias()})).source((new SearchSourceBuilder()).query(QueryBuilders.termQuery("key", key)).fetchSource("bpmnXml", (String)null));
         SearchResponse response = this.elasticsearch.search(searchRequest, RequestOptions.DEFAULT);
         if (response.getHits().getTotalHits().value == 1L) {
            Map result = response.getHits().getHits()[0].getSourceAsMap();
            return (String)result.get("bpmnXml");
         }
      } catch (IOException var5) {
         throw new ServerException(String.format("Error in reading process definition as xml for key %s", key), var5);
      }

      throw new ResourceNotFoundException(String.format("Process definition for key %s not found.", key));
   }

   protected void buildFiltering(Query query, SearchSourceBuilder searchSourceBuilder) {
      ProcessDefinition filter = (ProcessDefinition)query.getFilter();
      if (filter != null) {
         List queryBuilders = new ArrayList();
         queryBuilders.add(this.buildTermQuery("name", filter.getName()));
         queryBuilders.add(this.buildTermQuery("bpmnProcessId", filter.getBpmnProcessId()));
         queryBuilders.add(this.buildTermQuery("version", filter.getVersion()));
         queryBuilders.add(this.buildTermQuery("key", filter.getKey()));
         searchSourceBuilder.query(ElasticsearchUtil.joinWithAnd((QueryBuilder[])queryBuilders.toArray(new QueryBuilder[0])));
      }

   }

   protected List searchFor(SearchSourceBuilder searchSource) throws IOException {
      SearchRequest searchRequest = (new SearchRequest(new String[]{this.processIndex.getAlias()})).source(searchSource);
      return ElasticsearchUtil.scroll(searchRequest, ProcessDefinition.class, this.objectMapper, this.elasticsearch);
   }
}
