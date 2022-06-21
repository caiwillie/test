package io.camunda.operate.webapp.es.reader;

import io.camunda.operate.entities.ProcessEntity;
import io.camunda.operate.entities.listview.ProcessInstanceState;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.schema.templates.IncidentTemplate;
import io.camunda.operate.schema.templates.ListViewTemplate;
import io.camunda.operate.util.ConversionUtils;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.util.ElasticsearchUtil.QueryType;
import io.camunda.operate.webapp.rest.dto.incidents.IncidentByProcessStatisticsDto;
import io.camunda.operate.webapp.rest.dto.incidents.IncidentsByErrorMsgStatisticsDto;
import io.camunda.operate.webapp.rest.dto.incidents.IncidentsByProcessGroupStatisticsDto;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Cardinality;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IncidentStatisticsReader extends AbstractReader {
   private static final String ERROR_MESSAGE = "errorMessages";
   public static final String PROCESS_KEYS = "processDefinitionKeys";
   private static final String UNIQ_PROCESS_INSTANCES = "uniq_processInstances";
   private static final String GROUP_BY_ERROR_MESSAGE_HASH = "group_by_errorMessages";
   private static final String GROUP_BY_PROCESS_KEYS = "group_by_processDefinitionKeys";
   private static final Logger logger = LoggerFactory.getLogger(IncidentStatisticsReader.class);
   @Autowired
   private ListViewTemplate processInstanceTemplate;
   @Autowired
   private IncidentTemplate incidentTemplate;
   @Autowired
   private ProcessReader processReader;
   public static final AggregationBuilder COUNT_PROCESS_KEYS = ((TermsAggregationBuilder)AggregationBuilders.terms("processDefinitionKeys").field("processDefinitionKey")).size(10000);
   public static final QueryBuilder INCIDENTS_QUERY;

   public Set getProcessAndIncidentsStatistics() {
      Map incidentsByProcessMap = this.updateActiveInstances(this.getIncidentsByProcess());
      return this.collectStatisticsForProcessGroups(incidentsByProcessMap);
   }

   private Map getIncidentsByProcess() {
      Map results = new HashMap();
      SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest(this.processInstanceTemplate, QueryType.ONLY_RUNTIME).source((new SearchSourceBuilder()).query(INCIDENTS_QUERY).aggregation(COUNT_PROCESS_KEYS).size(0));

      try {
         SearchResponse searchResponse = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
         List buckets = ((Terms)searchResponse.getAggregations().get("processDefinitionKeys")).getBuckets();
         Iterator var5 = buckets.iterator();

         while(var5.hasNext()) {
            Terms.Bucket bucket = (Terms.Bucket)var5.next();
            Long processDefinitionKey = (Long)bucket.getKey();
            long incidents = bucket.getDocCount();
            results.put(processDefinitionKey, new IncidentByProcessStatisticsDto(processDefinitionKey.toString(), incidents, 0L));
         }

         return results;
      } catch (IOException var10) {
         String message = String.format("Exception occurred, while obtaining incidents by process: %s", var10.getMessage());
         logger.error(message, var10);
         throw new OperateRuntimeException(message, var10);
      }
   }

   private Map updateActiveInstances(Map statistics) {
      QueryBuilder runningInstanceQuery = ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{QueryBuilders.termQuery("state", ProcessInstanceState.ACTIVE.toString()), QueryBuilders.termQuery("joinRelation", "processInstance")});
      Map results = new HashMap(statistics);

      try {
         SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest(this.processInstanceTemplate, QueryType.ONLY_RUNTIME).source((new SearchSourceBuilder()).query(runningInstanceQuery).aggregation(COUNT_PROCESS_KEYS).size(0));
         SearchResponse searchResponse = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
         List buckets = ((Terms)searchResponse.getAggregations().get("processDefinitionKeys")).getBuckets();

         Long processDefinitionKey;
         IncidentByProcessStatisticsDto statistic;
         for(Iterator var7 = buckets.iterator(); var7.hasNext(); results.put(processDefinitionKey, statistic)) {
            Terms.Bucket bucket = (Terms.Bucket)var7.next();
            processDefinitionKey = (Long)bucket.getKey();
            long runningCount = bucket.getDocCount();
            statistic = (IncidentByProcessStatisticsDto)results.get(processDefinitionKey);
            if (statistic != null) {
               statistic.setActiveInstancesCount(runningCount - statistic.getInstancesWithActiveIncidentsCount());
            } else {
               statistic = new IncidentByProcessStatisticsDto(processDefinitionKey.toString(), 0L, runningCount);
            }
         }

         return results;
      } catch (IOException var13) {
         String message = String.format("Exception occurred, while obtaining active processes: %s", var13.getMessage());
         logger.error(message, var13);
         throw new OperateRuntimeException(message, var13);
      }
   }

   private Set collectStatisticsForProcessGroups(Map incidentsByProcessMap) {
      Set result = new TreeSet(IncidentsByProcessGroupStatisticsDto.COMPARATOR);
      Map processGroups = this.processReader.getProcessesGrouped();
      Iterator var4 = processGroups.entrySet().iterator();

      while(var4.hasNext()) {
         Map.Entry entry = (Map.Entry)var4.next();
         IncidentsByProcessGroupStatisticsDto stat = new IncidentsByProcessGroupStatisticsDto();
         stat.setBpmnProcessId((String)entry.getKey());
         long activeInstancesCount = 0L;
         long instancesWithActiveIncidentsCount = 0L;
         long maxVersion = 0L;
         Iterator var13 = ((List)entry.getValue()).iterator();

         while(var13.hasNext()) {
            ProcessEntity processEntity = (ProcessEntity)var13.next();
            IncidentByProcessStatisticsDto statForProcess = (IncidentByProcessStatisticsDto)incidentsByProcessMap.get(processEntity.getKey());
            if (statForProcess != null) {
               activeInstancesCount += statForProcess.getActiveInstancesCount();
               instancesWithActiveIncidentsCount += statForProcess.getInstancesWithActiveIncidentsCount();
            } else {
               statForProcess = new IncidentByProcessStatisticsDto(ConversionUtils.toStringOrNull(processEntity.getKey()), 0L, 0L);
            }

            statForProcess.setName(processEntity.getName());
            statForProcess.setBpmnProcessId(processEntity.getBpmnProcessId());
            statForProcess.setVersion(processEntity.getVersion());
            stat.getProcesses().add(statForProcess);
            if ((long)processEntity.getVersion() > maxVersion) {
               stat.setProcessName(processEntity.getName());
               maxVersion = (long)processEntity.getVersion();
            }
         }

         stat.setActiveInstancesCount(activeInstancesCount);
         stat.setInstancesWithActiveIncidentsCount(instancesWithActiveIncidentsCount);
         result.add(stat);
      }

      return result;
   }

   public Set getIncidentStatisticsByError() {
      Set result = new TreeSet(IncidentsByErrorMsgStatisticsDto.COMPARATOR);
      Map processes = this.processReader.getProcessesWithFields("key", "name", "bpmnProcessId", "version");
      TermsAggregationBuilder aggregation = (TermsAggregationBuilder)((TermsAggregationBuilder)((TermsAggregationBuilder)AggregationBuilders.terms("group_by_errorMessages").field("errorMessageHash")).size(10000).subAggregation(AggregationBuilders.topHits("errorMessages").size(1).fetchSource("errorMessage", (String)null))).subAggregation(((TermsAggregationBuilder)AggregationBuilders.terms("group_by_processDefinitionKeys").field("processDefinitionKey")).size(10000).subAggregation(AggregationBuilders.cardinality("uniq_processInstances").field("processInstanceKey")));
      SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest(this.incidentTemplate, QueryType.ONLY_RUNTIME).source((new SearchSourceBuilder()).query(IncidentTemplate.ACTIVE_INCIDENT_QUERY).aggregation(aggregation).size(0));

      try {
         SearchResponse searchResponse = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
         Terms errorMessageAggregation = (Terms)searchResponse.getAggregations().get("group_by_errorMessages");
         Iterator var7 = errorMessageAggregation.getBuckets().iterator();

         while(var7.hasNext()) {
            Terms.Bucket bucket = (Terms.Bucket)var7.next();
            result.add(this.getIncidentsByErrorMsgStatistic(processes, bucket));
         }

         return result;
      } catch (IOException var9) {
         String message = String.format("Exception occurred, while obtaining incidents by error message: %s", var9.getMessage());
         logger.error(message, var9);
         throw new OperateRuntimeException(message, var9);
      }
   }

   private IncidentsByErrorMsgStatisticsDto getIncidentsByErrorMsgStatistic(Map processes, Terms.Bucket errorMessageBucket) {
      SearchHits searchHits = ((TopHits)errorMessageBucket.getAggregations().get("errorMessages")).getHits();
      SearchHit searchHit = searchHits.getHits()[0];
      String errorMessage = (String)searchHit.getSourceAsMap().get("errorMessage");
      IncidentsByErrorMsgStatisticsDto processStatistics = new IncidentsByErrorMsgStatisticsDto(errorMessage);
      Terms processDefinitionKeyAggregation = (Terms)errorMessageBucket.getAggregations().get("group_by_processDefinitionKeys");

      long incidentsCount;
      for(Iterator var8 = processDefinitionKeyAggregation.getBuckets().iterator(); var8.hasNext(); processStatistics.recordInstancesCount(incidentsCount)) {
         Terms.Bucket processDefinitionKeyBucket = (Terms.Bucket)var8.next();
         Long processDefinitionKey = (Long)processDefinitionKeyBucket.getKey();
         incidentsCount = ((Cardinality)processDefinitionKeyBucket.getAggregations().get("uniq_processInstances")).getValue();
         if (processes.containsKey(processDefinitionKey)) {
            IncidentByProcessStatisticsDto statisticForProcess = new IncidentByProcessStatisticsDto(processDefinitionKey.toString(), errorMessage, incidentsCount);
            ProcessEntity process = (ProcessEntity)processes.get(processDefinitionKey);
            statisticForProcess.setName(process.getName());
            statisticForProcess.setBpmnProcessId(process.getBpmnProcessId());
            statisticForProcess.setVersion(process.getVersion());
            processStatistics.getProcesses().add(statisticForProcess);
         }
      }

      return processStatistics;
   }

   static {
      INCIDENTS_QUERY = ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{QueryBuilders.termQuery("joinRelation", "processInstance"), QueryBuilders.termQuery("state", ProcessInstanceState.ACTIVE.toString()), QueryBuilders.termQuery("incident", true)});
   }
}
