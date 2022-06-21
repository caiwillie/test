package io.camunda.operate.webapp.api.v1.dao;

import io.camunda.operate.schema.templates.VariableTemplate;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.webapp.api.v1.entities.Query;
import io.camunda.operate.webapp.api.v1.entities.Results;
import io.camunda.operate.webapp.api.v1.entities.Variable;
import io.camunda.operate.webapp.api.v1.exceptions.APIException;
import io.camunda.operate.webapp.api.v1.exceptions.ResourceNotFoundException;
import io.camunda.operate.webapp.api.v1.exceptions.ServerException;
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

@Component("ElasticsearchVariableDaoV1")
public class ElasticsearchVariableDao extends ElasticsearchDao implements VariableDao {
   @Autowired
   private VariableTemplate variableIndex;

   protected void buildFiltering(Query query, SearchSourceBuilder searchSourceBuilder) {
      Variable filter = (Variable)query.getFilter();
      List queryBuilders = new ArrayList();
      if (filter != null) {
         queryBuilders.add(this.buildTermQuery("key", filter.getKey()));
         queryBuilders.add(this.buildTermQuery("processInstanceKey", filter.getProcessInstanceKey()));
         queryBuilders.add(this.buildTermQuery("scopeKey", filter.getScopeKey()));
         queryBuilders.add(this.buildTermQuery("name", filter.getName()));
         queryBuilders.add(this.buildTermQuery("value", filter.getValue()));
         queryBuilders.add(this.buildTermQuery("isPreview", filter.getTruncated()));
      }

      searchSourceBuilder.query(ElasticsearchUtil.joinWithAnd((QueryBuilder[])queryBuilders.toArray(new QueryBuilder[0])));
   }

   public Variable byKey(Long key) throws APIException {
      this.logger.debug("byKey {}", key);

      List variables;
      try {
         variables = this.searchFor((new SearchSourceBuilder()).query(QueryBuilders.termQuery("key", key)));
      } catch (Exception var4) {
         throw new ServerException(String.format("Error in reading variable for key %s", key), var4);
      }

      if (variables.isEmpty()) {
         throw new ResourceNotFoundException(String.format("No variable found for key %s ", key));
      } else if (variables.size() > 1) {
         throw new ServerException(String.format("Found more than one variables for key %s", key));
      } else {
         return (Variable)variables.get(0);
      }
   }

   public Results search(Query query) throws APIException {
      this.logger.debug("search {}", query);
      SearchSourceBuilder searchSourceBuilder = this.buildQueryOn(query, "key", new SearchSourceBuilder());

      try {
         SearchRequest searchRequest = (new SearchRequest()).indices(new String[]{this.variableIndex.getAlias()}).source(searchSourceBuilder);
         SearchResponse searchResponse = this.elasticsearch.search(searchRequest, RequestOptions.DEFAULT);
         SearchHits searchHits = searchResponse.getHits();
         SearchHit[] searchHitArray = searchHits.getHits();
         if (searchHitArray != null && searchHitArray.length > 0) {
            Object[] sortValues = searchHitArray[searchHitArray.length - 1].getSortValues();
            List variables = ElasticsearchUtil.mapSearchHits(searchHitArray, this::searchHitToVariableWithoutFullValue);
            return (new Results()).setTotal(searchHits.getTotalHits().value).setItems(variables).setSortValues(sortValues);
         } else {
            return (new Results()).setTotal(searchHits.getTotalHits().value);
         }
      } catch (Exception var9) {
         throw new ServerException("Error in reading incidents", var9);
      }
   }

   protected Variable searchHitToVariableWithoutFullValue(SearchHit searchHit) {
      return this.searchHitToVariable(searchHit, false);
   }

   protected Variable searchHitToVariableWithFullValue(SearchHit searchHit) {
      return this.searchHitToVariable(searchHit, true);
   }

   protected Variable searchHitToVariable(SearchHit searchHit, boolean isFullValue) {
      Map searchHitAsMap = searchHit.getSourceAsMap();
      Variable variable = (new Variable()).setKey((Long)searchHitAsMap.get("key")).setProcessInstanceKey((Long)searchHitAsMap.get("processInstanceKey")).setScopeKey((Long)searchHitAsMap.get("scopeKey")).setName((String)searchHitAsMap.get("name")).setValue((String)searchHitAsMap.get("value")).setTruncated((Boolean)searchHitAsMap.get("isPreview"));
      if (isFullValue) {
         String fullValue = (String)searchHitAsMap.get("fullValue");
         if (fullValue != null) {
            variable.setValue(fullValue);
         }

         variable.setTruncated(false);
      }

      return variable;
   }

   protected List searchFor(SearchSourceBuilder searchSourceBuilder) {
      try {
         SearchRequest searchRequest = (new SearchRequest(new String[]{this.variableIndex.getAlias()})).source(searchSourceBuilder);
         SearchResponse searchResponse = this.elasticsearch.search(searchRequest, RequestOptions.DEFAULT);
         SearchHits searchHits = searchResponse.getHits();
         SearchHit[] searchHitArray = searchHits.getHits();
         return searchHitArray != null && searchHitArray.length > 0 ? ElasticsearchUtil.mapSearchHits(searchHitArray, this::searchHitToVariableWithFullValue) : List.of();
      } catch (Exception var6) {
         throw new ServerException("Error in reading variables", var6);
      }
   }
}
