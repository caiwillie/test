package io.camunda.operate.webapp.api.v1.dao;

import io.camunda.operate.schema.templates.FlowNodeInstanceTemplate;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.webapp.api.v1.entities.FlowNodeInstance;
import io.camunda.operate.webapp.api.v1.entities.Query;
import io.camunda.operate.webapp.api.v1.entities.Results;
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

@Component("ElasticsearchFlowNodeInstanceDaoV1")
public class ElasticsearchFlowNodeInstanceDao extends ElasticsearchDao implements FlowNodeInstanceDao {
   @Autowired
   private FlowNodeInstanceTemplate flowNodeInstanceIndex;

   protected void buildFiltering(Query query, SearchSourceBuilder searchSourceBuilder) {
      FlowNodeInstance filter = (FlowNodeInstance)query.getFilter();
      List queryBuilders = new ArrayList();
      if (filter != null) {
         queryBuilders.add(this.buildTermQuery("key", filter.getKey()));
         queryBuilders.add(this.buildTermQuery("processInstanceKey", filter.getProcessInstanceKey()));
         queryBuilders.add(this.buildMatchDateQuery("startDate", filter.getStartDate()));
         queryBuilders.add(this.buildMatchDateQuery("endDate", filter.getEndDate()));
         queryBuilders.add(this.buildTermQuery("state", filter.getState()));
         queryBuilders.add(this.buildTermQuery("type", filter.getType()));
         queryBuilders.add(this.buildTermQuery("incident", filter.getIncident()));
         queryBuilders.add(this.buildTermQuery("incidentKey", filter.getIncidentKey()));
      }

      searchSourceBuilder.query(ElasticsearchUtil.joinWithAnd((QueryBuilder[])queryBuilders.toArray(new QueryBuilder[0])));
   }

   public FlowNodeInstance byKey(Long key) throws APIException {
      this.logger.debug("byKey {}", key);

      List flowNodeInstances;
      try {
         flowNodeInstances = this.searchFor((new SearchSourceBuilder()).query(QueryBuilders.termQuery("key", key)));
      } catch (Exception var4) {
         throw new ServerException(String.format("Error in reading flownode instance for key %s", key), var4);
      }

      if (flowNodeInstances.isEmpty()) {
         throw new ResourceNotFoundException(String.format("No flownode instance found for key %s ", key));
      } else if (flowNodeInstances.size() > 1) {
         throw new ServerException(String.format("Found more than one flownode instances for key %s", key));
      } else {
         return (FlowNodeInstance)flowNodeInstances.get(0);
      }
   }

   public Results search(Query query) throws APIException {
      this.logger.debug("search {}", query);
      SearchSourceBuilder searchSourceBuilder = this.buildQueryOn(query, "key", new SearchSourceBuilder());

      try {
         SearchRequest searchRequest = (new SearchRequest()).indices(new String[]{this.flowNodeInstanceIndex.getAlias()}).source(searchSourceBuilder);
         SearchResponse searchResponse = this.elasticsearch.search(searchRequest, RequestOptions.DEFAULT);
         SearchHits searchHits = searchResponse.getHits();
         SearchHit[] searchHitArray = searchHits.getHits();
         if (searchHitArray != null && searchHitArray.length > 0) {
            Object[] sortValues = searchHitArray[searchHitArray.length - 1].getSortValues();
            List flowNodeInstances = ElasticsearchUtil.mapSearchHits(searchHitArray, this::searchHitToIncident);
            return (new Results()).setTotal(searchHits.getTotalHits().value).setItems(flowNodeInstances).setSortValues(sortValues);
         } else {
            return (new Results()).setTotal(searchHits.getTotalHits().value);
         }
      } catch (Exception var9) {
         throw new ServerException("Error in reading flownode instances", var9);
      }
   }

   private FlowNodeInstance searchHitToIncident(SearchHit searchHit) {
      Map searchHitAsMap = searchHit.getSourceAsMap();
      return (new FlowNodeInstance()).setKey((Long)searchHitAsMap.get("key")).setProcessInstanceKey((Long)searchHitAsMap.get("processInstanceKey")).setStartDate((String)searchHitAsMap.get("startDate")).setEndDate((String)searchHitAsMap.get("endDate")).setType((String)searchHitAsMap.get("type")).setState((String)searchHitAsMap.get("state")).setIncident((Boolean)searchHitAsMap.get("incident")).setIncidentKey((Long)searchHitAsMap.get("incidentKey"));
   }

   protected List searchFor(SearchSourceBuilder searchSourceBuilder) {
      try {
         SearchRequest searchRequest = (new SearchRequest(new String[]{this.flowNodeInstanceIndex.getAlias()})).source(searchSourceBuilder);
         SearchResponse searchResponse = this.elasticsearch.search(searchRequest, RequestOptions.DEFAULT);
         SearchHits searchHits = searchResponse.getHits();
         SearchHit[] searchHitArray = searchHits.getHits();
         return searchHitArray != null && searchHitArray.length > 0 ? ElasticsearchUtil.mapSearchHits(searchHitArray, this::searchHitToIncident) : List.of();
      } catch (Exception var6) {
         throw new ServerException("Error in reading incidents", var6);
      }
   }
}
