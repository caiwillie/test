package io.camunda.operate.webapp.api.v1.dao;

import io.camunda.operate.schema.templates.ListViewTemplate;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.webapp.api.v1.entities.ChangeStatus;
import io.camunda.operate.webapp.api.v1.entities.ProcessInstance;
import io.camunda.operate.webapp.api.v1.entities.Query;
import io.camunda.operate.webapp.api.v1.entities.Results;
import io.camunda.operate.webapp.api.v1.exceptions.APIException;
import io.camunda.operate.webapp.api.v1.exceptions.ClientException;
import io.camunda.operate.webapp.api.v1.exceptions.ResourceNotFoundException;
import io.camunda.operate.webapp.api.v1.exceptions.ServerException;
import io.camunda.operate.webapp.es.writer.ProcessInstanceWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

@Component("ElasticsearchProcessInstanceDaoV1")
public class ElasticsearchProcessInstanceDao extends ElasticsearchDao implements ProcessInstanceDao {
   @Autowired
   private ListViewTemplate processInstanceIndex;
   @Autowired
   private ProcessInstanceWriter processInstanceWriter;

   public Results search(Query query) throws APIException {
      this.logger.debug("search {}", query);
      SearchSourceBuilder searchSourceBuilder = this.buildQueryOn(query, "processInstanceKey", new SearchSourceBuilder());

      try {
         SearchRequest searchRequest = (new SearchRequest()).indices(new String[]{this.processInstanceIndex.getAlias()}).source(searchSourceBuilder);
         SearchResponse searchResponse = this.elasticsearch.search(searchRequest, RequestOptions.DEFAULT);
         SearchHits searchHits = searchResponse.getHits();
         SearchHit[] searchHitArray = searchHits.getHits();
         if (searchHitArray != null && searchHitArray.length > 0) {
            Object[] sortValues = searchHitArray[searchHitArray.length - 1].getSortValues();
            List processInstances = ElasticsearchUtil.mapSearchHits(searchHitArray, this.objectMapper, ProcessInstance.class);
            return (new Results()).setTotal(searchHits.getTotalHits().value).setItems(processInstances).setSortValues(sortValues);
         } else {
            return (new Results()).setTotal(searchHits.getTotalHits().value);
         }
      } catch (Exception var9) {
         throw new ServerException("Error in reading process instances", var9);
      }
   }

   public ProcessInstance byKey(Long key) throws APIException {
      this.logger.debug("byKey {}", key);

      List processInstances;
      try {
         processInstances = this.searchFor((new SearchSourceBuilder()).query(QueryBuilders.termQuery("key", key)));
      } catch (Exception var4) {
         throw new ServerException(String.format("Error in reading process instance for key %s", key), var4);
      }

      if (processInstances.isEmpty()) {
         throw new ResourceNotFoundException(String.format("No process instances found for key %s ", key));
      } else if (processInstances.size() > 1) {
         throw new ServerException(String.format("Found more than one process instances for key %s", key));
      } else {
         return (ProcessInstance)processInstances.get(0);
      }
   }

   public ChangeStatus delete(Long key) throws APIException {
      this.byKey(key);

      try {
         this.processInstanceWriter.deleteInstanceById(key);
         return (new ChangeStatus()).setDeleted(1L).setMessage(String.format("Process instance and dependant data deleted for key '%s'", key));
      } catch (IllegalArgumentException var3) {
         throw new ClientException(var3.getMessage(), var3);
      } catch (Exception var4) {
         throw new ServerException(String.format("Error in deleting process instance and dependant data for key '%s'", key), var4);
      }
   }

   protected void buildFiltering(Query query, SearchSourceBuilder searchSourceBuilder) {
      ProcessInstance filter = (ProcessInstance)query.getFilter();
      List queryBuilders = new ArrayList();
      queryBuilders.add(QueryBuilders.termQuery("joinRelation", "processInstance"));
      if (filter != null) {
         queryBuilders.add(this.buildTermQuery("processInstanceKey", filter.getKey()));
         queryBuilders.add(this.buildTermQuery("processDefinitionKey", filter.getProcessDefinitionKey()));
         queryBuilders.add(this.buildTermQuery("parentProcessInstanceKey", filter.getParentKey()));
         queryBuilders.add(this.buildTermQuery("processVersion", filter.getProcessVersion()));
         queryBuilders.add(this.buildTermQuery("bpmnProcessId", filter.getBpmnProcessId()));
         queryBuilders.add(this.buildTermQuery("state", filter.getState()));
         queryBuilders.add(this.buildMatchDateQuery("startDate", filter.getStartDate()));
         queryBuilders.add(this.buildMatchDateQuery("endDate", filter.getEndDate()));
      }

      searchSourceBuilder.query(ElasticsearchUtil.joinWithAnd((QueryBuilder[])queryBuilders.toArray(new QueryBuilder[0])));
   }

   protected List searchFor(SearchSourceBuilder searchSource) throws IOException {
      SearchRequest searchRequest = (new SearchRequest(new String[]{this.processInstanceIndex.getAlias()})).source(searchSource);
      return ElasticsearchUtil.scroll(searchRequest, ProcessInstance.class, this.objectMapper, this.elasticsearch);
   }
}
