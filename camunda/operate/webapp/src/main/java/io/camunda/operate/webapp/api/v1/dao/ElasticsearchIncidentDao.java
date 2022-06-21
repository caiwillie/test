package io.camunda.operate.webapp.api.v1.dao;

import io.camunda.operate.schema.templates.IncidentTemplate;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.webapp.api.v1.entities.Incident;
import io.camunda.operate.webapp.api.v1.entities.Query;
import io.camunda.operate.webapp.api.v1.entities.Results;
import io.camunda.operate.webapp.api.v1.exceptions.APIException;
import io.camunda.operate.webapp.api.v1.exceptions.ResourceNotFoundException;
import io.camunda.operate.webapp.api.v1.exceptions.ServerException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

@Component("ElasticsearchIncidentDaoV1")
public class ElasticsearchIncidentDao extends ElasticsearchDao implements IncidentDao {
   @Autowired
   private IncidentTemplate incidentIndex;

   protected void buildFiltering(Query query, SearchSourceBuilder searchSourceBuilder) {
      Incident filter = (Incident)query.getFilter();
      List queryBuilders = new ArrayList();
      if (filter != null) {
         queryBuilders.add(this.buildTermQuery("key", filter.getKey()));
         queryBuilders.add(this.buildTermQuery("processDefinitionKey", filter.getProcessDefinitionKey()));
         queryBuilders.add(this.buildTermQuery("processInstanceKey", filter.getProcessInstanceKey()));
         queryBuilders.add(this.buildTermQuery("errorType", filter.getType()));
         queryBuilders.add(this.buildMatchQuery("errorMessage", filter.getMessage()));
         queryBuilders.add(this.buildTermQuery("state", filter.getState()));
         queryBuilders.add(this.buildMatchDateQuery("creationTime", filter.getCreationTime()));
      }

      searchSourceBuilder.query(ElasticsearchUtil.joinWithAnd((QueryBuilder[])queryBuilders.toArray(new QueryBuilder[0])));
   }

   public Incident byKey(Long key) throws APIException {
      this.logger.debug("byKey {}", key);

      List incidents;
      try {
         incidents = this.searchFor((new SearchSourceBuilder()).query(QueryBuilders.termQuery("key", key)));
      } catch (Exception var4) {
         throw new ServerException(String.format("Error in reading incident for key %s", key), var4);
      }

      if (incidents.isEmpty()) {
         throw new ResourceNotFoundException(String.format("No incident found for key %s ", key));
      } else if (incidents.size() > 1) {
         throw new ServerException(String.format("Found more than one incidents for key %s", key));
      } else {
         return (Incident)incidents.get(0);
      }
   }

   public Results search(Query query) throws APIException {
      this.logger.debug("search {}", query);
      this.mapFieldsInSort(query);
      SearchSourceBuilder searchSourceBuilder = this.buildQueryOn(query, "key", new SearchSourceBuilder());

      try {
         SearchRequest searchRequest = (new SearchRequest()).indices(new String[]{this.incidentIndex.getAlias()}).source(searchSourceBuilder);
         SearchResponse searchResponse = this.elasticsearch.search(searchRequest, RequestOptions.DEFAULT);
         SearchHits searchHits = searchResponse.getHits();
         SearchHit[] searchHitArray = searchHits.getHits();
         if (searchHitArray != null && searchHitArray.length > 0) {
            Object[] sortValues = searchHitArray[searchHitArray.length - 1].getSortValues();
            List incidents = ElasticsearchUtil.mapSearchHits(searchHitArray, this::searchHitToIncident);
            return (new Results()).setTotal(searchHits.getTotalHits().value).setItems(incidents).setSortValues(sortValues);
         } else {
            return (new Results()).setTotal(searchHits.getTotalHits().value);
         }
      } catch (Exception var9) {
         throw new ServerException("Error in reading incidents", var9);
      }
   }

   private void mapFieldsInSort(Query query) {
      if (query.getSort() != null) {
         query.setSort((List)query.getSort().stream().map((s) -> {
            return s.setField((String)Incident.OBJECT_TO_ELASTICSEARCH.getOrDefault(s.getField(), s.getField()));
         }).collect(Collectors.toList()));
      }
   }

   protected Incident searchHitToIncident(SearchHit searchHit) {
      Map searchHitAsMap = searchHit.getSourceAsMap();
      return (new Incident()).setKey((Long)searchHitAsMap.get("key")).setProcessInstanceKey((Long)searchHitAsMap.get("processInstanceKey")).setProcessDefinitionKey((Long)searchHitAsMap.get("processDefinitionKey")).setType((String)searchHitAsMap.get("errorType")).setMessage((String)searchHitAsMap.get("errorMessage")).setCreationTime((String)searchHitAsMap.get("creationTime")).setState((String)searchHitAsMap.get("state"));
   }

   protected List searchFor(SearchSourceBuilder searchSourceBuilder) {
      try {
         SearchRequest searchRequest = (new SearchRequest(new String[]{this.incidentIndex.getAlias()})).source(searchSourceBuilder);
         SearchResponse searchResponse = this.elasticsearch.search(searchRequest, RequestOptions.DEFAULT);
         SearchHits searchHits = searchResponse.getHits();
         SearchHit[] searchHitArray = searchHits.getHits();
         return searchHitArray != null && searchHitArray.length > 0 ? ElasticsearchUtil.mapSearchHits(searchHitArray, this::searchHitToIncident) : List.of();
      } catch (Exception var6) {
         throw new ServerException("Error in reading incidents", var6);
      }
   }
}
