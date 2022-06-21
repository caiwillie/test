package io.camunda.operate.webapp.es.reader;

import io.camunda.operate.entities.listview.ProcessInstanceForListViewEntity;
import io.camunda.operate.entities.listview.ProcessInstanceState;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.schema.templates.IncidentTemplate;
import io.camunda.operate.schema.templates.ListViewTemplate;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.util.ElasticsearchUtil.QueryType;
import io.camunda.operate.webapp.rest.dto.ProcessInstanceCoreStatisticsDto;
import io.camunda.operate.webapp.rest.dto.ProcessInstanceReferenceDto;
import io.camunda.operate.webapp.rest.dto.listview.ListViewProcessInstanceDto;
import io.camunda.operate.webapp.rest.exception.NotFoundException;
import io.camunda.operate.zeebeimport.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.SingleBucketAggregation;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcessInstanceReader extends AbstractReader {
   private static final Logger logger = LoggerFactory.getLogger(ProcessInstanceReader.class);
   public static final FilterAggregationBuilder INCIDENTS_AGGREGATION = AggregationBuilders.filter("incidents", ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{QueryBuilders.termQuery("incident", true), QueryBuilders.termQuery("joinRelation", "processInstance")}));
   public static final FilterAggregationBuilder RUNNING_AGGREGATION;
   @Autowired
   private ListViewTemplate listViewTemplate;
   @Autowired
   private IncidentTemplate incidentTemplate;
   @Autowired
   private OperationReader operationReader;

   public ListViewProcessInstanceDto getProcessInstanceWithOperationsByKey(Long processInstanceKey) {
      try {
         ProcessInstanceForListViewEntity processInstance = this.searchProcessInstanceByKey(processInstanceKey);
         List callHierarchy = this.createCallHierarchy(processInstance.getTreePath(), String.valueOf(processInstanceKey));
         return ListViewProcessInstanceDto.createFrom(processInstance, this.operationReader.getOperationsByProcessInstanceKey(processInstanceKey), callHierarchy);
      } catch (IOException var4) {
         String message = String.format("Exception occurred, while obtaining process instance with operations: %s", var4.getMessage());
         logger.error(message, var4);
         throw new OperateRuntimeException(message, var4);
      }
   }

   private List createCallHierarchy(String treePath, String currentProcessInstanceId) {
      List callHierarchy = new ArrayList();
      List processInstanceIds = (new TreePath(treePath)).extractProcessInstanceIds();
      processInstanceIds.remove(currentProcessInstanceId);
      QueryBuilder query = ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{QueryBuilders.termQuery("joinRelation", "processInstance"), QueryBuilders.termsQuery("id", processInstanceIds)});
      SearchRequest request = ElasticsearchUtil.createSearchRequest(this.listViewTemplate).source((new SearchSourceBuilder()).query(query).fetchSource(new String[]{"id", "processDefinitionKey", "processName", "bpmnProcessId"}, (String[])null));

      try {
         ElasticsearchUtil.scrollWith(request, this.esClient, (searchHits) -> {
            Arrays.stream(searchHits.getHits()).forEach((sh) -> {
               Map source = sh.getSourceAsMap();
               callHierarchy.add((new ProcessInstanceReferenceDto()).setInstanceId(String.valueOf(source.get("id"))).setProcessDefinitionId(String.valueOf(source.get("processDefinitionKey"))).setProcessDefinitionName(String.valueOf(source.getOrDefault("processName", source.get("bpmnProcessId")))));
            });
         });
      } catch (IOException var9) {
         String message = String.format("Exception occurred, while obtaining process instance call hierarchy: %s", var9.getMessage());
         throw new OperateRuntimeException(message, var9);
      }

      callHierarchy.sort(Comparator.comparing((ref) -> {
         return processInstanceIds.indexOf(ref.getInstanceId());
      }));
      return callHierarchy;
   }

   public ProcessInstanceForListViewEntity getProcessInstanceByKey(Long processInstanceKey) {
      try {
         return this.searchProcessInstanceByKey(processInstanceKey);
      } catch (IOException var4) {
         String message = String.format("Exception occurred, while obtaining process instance: %s", var4.getMessage());
         logger.error(message, var4);
         throw new OperateRuntimeException(message, var4);
      }
   }

   protected ProcessInstanceForListViewEntity searchProcessInstanceByKey(Long processInstanceKey) throws IOException {
      QueryBuilder query = ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{QueryBuilders.idsQuery().addIds(new String[]{String.valueOf(processInstanceKey)}), QueryBuilders.termQuery("processInstanceKey", processInstanceKey)});
      SearchRequest request = ElasticsearchUtil.createSearchRequest(this.listViewTemplate, QueryType.ALL).source((new SearchSourceBuilder()).query(QueryBuilders.constantScoreQuery(query)));
      SearchResponse response = this.esClient.search(request, RequestOptions.DEFAULT);
      if (response.getHits().getTotalHits().value == 1L) {
         ProcessInstanceForListViewEntity processInstance = (ProcessInstanceForListViewEntity)ElasticsearchUtil.fromSearchHit(response.getHits().getHits()[0].getSourceAsString(), this.objectMapper, ProcessInstanceForListViewEntity.class);
         return processInstance;
      } else if (response.getHits().getTotalHits().value > 1L) {
         throw new NotFoundException(String.format("Could not find unique process instance with id '%s'.", processInstanceKey));
      } else {
         throw new NotFoundException(String.format("Could not find process instance with id '%s'.", processInstanceKey));
      }
   }

   public ProcessInstanceCoreStatisticsDto getCoreStatistics() {
      SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest(this.listViewTemplate, QueryType.ONLY_RUNTIME).source((new SearchSourceBuilder()).size(0).aggregation(INCIDENTS_AGGREGATION).aggregation(RUNNING_AGGREGATION));

      try {
         SearchResponse response = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
         Aggregations aggregations = response.getAggregations();
         long runningCount = ((SingleBucketAggregation)aggregations.get("running")).getDocCount();
         long incidentCount = ((SingleBucketAggregation)aggregations.get("incidents")).getDocCount();
         ProcessInstanceCoreStatisticsDto processInstanceCoreStatisticsDto = (new ProcessInstanceCoreStatisticsDto()).setRunning(runningCount).setActive(runningCount - incidentCount).setWithIncidents(incidentCount);
         return processInstanceCoreStatisticsDto;
      } catch (IOException var9) {
         String message = String.format("Exception occurred, while obtaining process instance core statistics: %s", var9.getMessage());
         logger.error(message, var9);
         throw new OperateRuntimeException(message, var9);
      }
   }

   public String getProcessInstanceTreePath(String processInstanceId) {
      QueryBuilder query = ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{QueryBuilders.termQuery("joinRelation", "processInstance"), QueryBuilders.termQuery("key", processInstanceId)});
      SearchRequest request = ElasticsearchUtil.createSearchRequest(this.listViewTemplate).source((new SearchSourceBuilder()).query(query).fetchSource("treePath", (String)null));

      try {
         SearchResponse response = this.esClient.search(request, RequestOptions.DEFAULT);
         if (response.getHits().getTotalHits().value > 0L) {
            return (String)response.getHits().getAt(0).getSourceAsMap().get("treePath");
         } else {
            throw new OperateRuntimeException(String.format("Process instance not found: %s", processInstanceId));
         }
      } catch (IOException var6) {
         String message = String.format("Exception occurred, while obtaining tree path for process instance: %s", var6.getMessage());
         throw new OperateRuntimeException(message, var6);
      }
   }

   static {
      RUNNING_AGGREGATION = AggregationBuilders.filter("running", QueryBuilders.termQuery("state", ProcessInstanceState.ACTIVE));
   }
}
