/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.ProcessEntity
 *  io.camunda.operate.entities.listview.ProcessInstanceState
 *  io.camunda.operate.exceptions.OperateRuntimeException
 *  io.camunda.operate.schema.templates.IncidentTemplate
 *  io.camunda.operate.schema.templates.ListViewTemplate
 *  io.camunda.operate.schema.templates.TemplateDescriptor
 *  io.camunda.operate.util.ConversionUtils
 *  io.camunda.operate.util.ElasticsearchUtil
 *  io.camunda.operate.util.ElasticsearchUtil$QueryType
 *  io.camunda.operate.webapp.es.reader.AbstractReader
 *  io.camunda.operate.webapp.es.reader.ProcessReader
 *  io.camunda.operate.webapp.rest.dto.incidents.IncidentByProcessStatisticsDto
 *  io.camunda.operate.webapp.rest.dto.incidents.IncidentsByErrorMsgStatisticsDto
 *  io.camunda.operate.webapp.rest.dto.incidents.IncidentsByProcessGroupStatisticsDto
 *  org.elasticsearch.action.search.SearchRequest
 *  org.elasticsearch.action.search.SearchResponse
 *  org.elasticsearch.client.RequestOptions
 *  org.elasticsearch.index.query.QueryBuilder
 *  org.elasticsearch.index.query.QueryBuilders
 *  org.elasticsearch.search.SearchHit
 *  org.elasticsearch.search.SearchHits
 *  org.elasticsearch.search.aggregations.AggregationBuilder
 *  org.elasticsearch.search.aggregations.AggregationBuilders
 *  org.elasticsearch.search.aggregations.bucket.terms.Terms
 *  org.elasticsearch.search.aggregations.bucket.terms.Terms$Bucket
 *  org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder
 *  org.elasticsearch.search.aggregations.metrics.Cardinality
 *  org.elasticsearch.search.aggregations.metrics.TopHits
 *  org.elasticsearch.search.builder.SearchSourceBuilder
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.es.reader;

import io.camunda.operate.entities.ProcessEntity;
import io.camunda.operate.entities.listview.ProcessInstanceState;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.schema.templates.IncidentTemplate;
import io.camunda.operate.schema.templates.ListViewTemplate;
import io.camunda.operate.schema.templates.TemplateDescriptor;
import io.camunda.operate.util.ConversionUtils;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.webapp.es.reader.AbstractReader;
import io.camunda.operate.webapp.es.reader.ProcessReader;
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
public class IncidentStatisticsReader
extends AbstractReader {
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
    public static final AggregationBuilder COUNT_PROCESS_KEYS = ((TermsAggregationBuilder)AggregationBuilders.terms((String)"processDefinitionKeys").field("processDefinitionKey")).size(10000);
    public static final QueryBuilder INCIDENTS_QUERY = ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{QueryBuilders.termQuery((String)"joinRelation", (String)"processInstance"), QueryBuilders.termQuery((String)"state", (String)ProcessInstanceState.ACTIVE.toString()), QueryBuilders.termQuery((String)"incident", (boolean)true)});

    public Set<IncidentsByProcessGroupStatisticsDto> getProcessAndIncidentsStatistics() {
        Map<Long, IncidentByProcessStatisticsDto> incidentsByProcessMap = this.updateActiveInstances(this.getIncidentsByProcess());
        return this.collectStatisticsForProcessGroups(incidentsByProcessMap);
    }

    private Map<Long, IncidentByProcessStatisticsDto> getIncidentsByProcess() {
        HashMap<Long, IncidentByProcessStatisticsDto> results = new HashMap<Long, IncidentByProcessStatisticsDto>();
        SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.processInstanceTemplate, (ElasticsearchUtil.QueryType)ElasticsearchUtil.QueryType.ONLY_RUNTIME).source(new SearchSourceBuilder().query(INCIDENTS_QUERY).aggregation(COUNT_PROCESS_KEYS).size(0));
        try {
            SearchResponse searchResponse = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
            List buckets = ((Terms)searchResponse.getAggregations().get(PROCESS_KEYS)).getBuckets();
            Iterator iterator = buckets.iterator();
            while (iterator.hasNext()) {
                Terms.Bucket bucket = (Terms.Bucket)iterator.next();
                Long processDefinitionKey = (Long)bucket.getKey();
                long incidents = bucket.getDocCount();
                results.put(processDefinitionKey, new IncidentByProcessStatisticsDto(processDefinitionKey.toString(), incidents, 0L));
            }
            return results;
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining incidents by process: %s", e.getMessage());
            logger.error(message, e);
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    private Map<Long, IncidentByProcessStatisticsDto> updateActiveInstances(Map<Long, IncidentByProcessStatisticsDto> statistics) {
        QueryBuilder runningInstanceQuery = ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{QueryBuilders.termQuery((String)"state", (String)ProcessInstanceState.ACTIVE.toString()), QueryBuilders.termQuery((String)"joinRelation", (String)"processInstance")});
        HashMap<Long, IncidentByProcessStatisticsDto> results = new HashMap<Long, IncidentByProcessStatisticsDto>(statistics);
        try {
            SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.processInstanceTemplate, (ElasticsearchUtil.QueryType)ElasticsearchUtil.QueryType.ONLY_RUNTIME).source(new SearchSourceBuilder().query(runningInstanceQuery).aggregation(COUNT_PROCESS_KEYS).size(0));
            SearchResponse searchResponse = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
            List buckets = ((Terms)searchResponse.getAggregations().get(PROCESS_KEYS)).getBuckets();
            Iterator iterator = buckets.iterator();
            while (iterator.hasNext()) {
                Terms.Bucket bucket = (Terms.Bucket)iterator.next();
                Long processDefinitionKey = (Long)bucket.getKey();
                long runningCount = bucket.getDocCount();
                IncidentByProcessStatisticsDto statistic = (IncidentByProcessStatisticsDto)results.get(processDefinitionKey);
                if (statistic != null) {
                    statistic.setActiveInstancesCount(runningCount - statistic.getInstancesWithActiveIncidentsCount());
                } else {
                    statistic = new IncidentByProcessStatisticsDto(processDefinitionKey.toString(), 0L, runningCount);
                }
                results.put(processDefinitionKey, statistic);
            }
            return results;
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining active processes: %s", e.getMessage());
            logger.error(message, e);
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    private Set<IncidentsByProcessGroupStatisticsDto> collectStatisticsForProcessGroups(Map<Long, IncidentByProcessStatisticsDto> incidentsByProcessMap) {
        TreeSet<IncidentsByProcessGroupStatisticsDto> result = new TreeSet<IncidentsByProcessGroupStatisticsDto>(IncidentsByProcessGroupStatisticsDto.COMPARATOR);
        Map<String, List<ProcessEntity>> processGroups = this.processReader.getProcessesGrouped();
        Iterator<Map.Entry<String, List<ProcessEntity>>> iterator = processGroups.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<ProcessEntity>> entry = iterator.next();
            IncidentsByProcessGroupStatisticsDto stat = new IncidentsByProcessGroupStatisticsDto();
            stat.setBpmnProcessId((String)entry.getKey());
            long activeInstancesCount = 0L;
            long instancesWithActiveIncidentsCount = 0L;
            long maxVersion = 0L;
            for (ProcessEntity processEntity : entry.getValue()) {
                IncidentByProcessStatisticsDto statForProcess = incidentsByProcessMap.get(processEntity.getKey());
                if (statForProcess != null) {
                    activeInstancesCount += statForProcess.getActiveInstancesCount();
                    instancesWithActiveIncidentsCount += statForProcess.getInstancesWithActiveIncidentsCount();
                } else {
                    statForProcess = new IncidentByProcessStatisticsDto(ConversionUtils.toStringOrNull((Object)processEntity.getKey()), 0L, 0L);
                }
                statForProcess.setName(processEntity.getName());
                statForProcess.setBpmnProcessId(processEntity.getBpmnProcessId());
                statForProcess.setVersion(processEntity.getVersion());
                stat.getProcesses().add(statForProcess);
                if ((long)processEntity.getVersion() <= maxVersion) continue;
                stat.setProcessName(processEntity.getName());
                maxVersion = processEntity.getVersion();
            }
            stat.setActiveInstancesCount(activeInstancesCount);
            stat.setInstancesWithActiveIncidentsCount(instancesWithActiveIncidentsCount);
            result.add(stat);
        }
        return result;
    }

    public Set<IncidentsByErrorMsgStatisticsDto> getIncidentStatisticsByError() {
        TreeSet<IncidentsByErrorMsgStatisticsDto> result = new TreeSet<IncidentsByErrorMsgStatisticsDto>(IncidentsByErrorMsgStatisticsDto.COMPARATOR);
        Map processes = this.processReader.getProcessesWithFields(new String[]{"key", "name", "bpmnProcessId", "version"});
        TermsAggregationBuilder aggregation = (TermsAggregationBuilder)((TermsAggregationBuilder)((TermsAggregationBuilder)AggregationBuilders.terms((String)GROUP_BY_ERROR_MESSAGE_HASH).field("errorMessageHash")).size(10000).subAggregation((AggregationBuilder)AggregationBuilders.topHits((String)ERROR_MESSAGE).size(1).fetchSource("errorMessage", null))).subAggregation((AggregationBuilder)((TermsAggregationBuilder)AggregationBuilders.terms((String)GROUP_BY_PROCESS_KEYS).field("processDefinitionKey")).size(10000).subAggregation((AggregationBuilder)AggregationBuilders.cardinality((String)UNIQ_PROCESS_INSTANCES).field("processInstanceKey")));
        SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.incidentTemplate, (ElasticsearchUtil.QueryType)ElasticsearchUtil.QueryType.ONLY_RUNTIME).source(new SearchSourceBuilder().query(IncidentTemplate.ACTIVE_INCIDENT_QUERY).aggregation((AggregationBuilder)aggregation).size(0));
        try {
            SearchResponse searchResponse = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
            Terms errorMessageAggregation = (Terms)searchResponse.getAggregations().get(GROUP_BY_ERROR_MESSAGE_HASH);
            Iterator iterator = errorMessageAggregation.getBuckets().iterator();
            while (iterator.hasNext()) {
                Terms.Bucket bucket = (Terms.Bucket)iterator.next();
                result.add(this.getIncidentsByErrorMsgStatistic(processes, bucket));
            }
            return result;
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining incidents by error message: %s", e.getMessage());
            logger.error(message, e);
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    private IncidentsByErrorMsgStatisticsDto getIncidentsByErrorMsgStatistic(Map<Long, ProcessEntity> processes, Terms.Bucket errorMessageBucket) {
        SearchHits searchHits = ((TopHits)errorMessageBucket.getAggregations().get(ERROR_MESSAGE)).getHits();
        SearchHit searchHit = searchHits.getHits()[0];
        String errorMessage = (String)searchHit.getSourceAsMap().get("errorMessage");
        IncidentsByErrorMsgStatisticsDto processStatistics = new IncidentsByErrorMsgStatisticsDto(errorMessage);
        Terms processDefinitionKeyAggregation = (Terms)errorMessageBucket.getAggregations().get(GROUP_BY_PROCESS_KEYS);
        Iterator iterator = processDefinitionKeyAggregation.getBuckets().iterator();
        while (iterator.hasNext()) {
            Terms.Bucket processDefinitionKeyBucket = (Terms.Bucket)iterator.next();
            Long processDefinitionKey = (Long)processDefinitionKeyBucket.getKey();
            long incidentsCount = ((Cardinality)processDefinitionKeyBucket.getAggregations().get(UNIQ_PROCESS_INSTANCES)).getValue();
            if (processes.containsKey(processDefinitionKey)) {
                IncidentByProcessStatisticsDto statisticForProcess = new IncidentByProcessStatisticsDto(processDefinitionKey.toString(), errorMessage, incidentsCount);
                ProcessEntity process = processes.get(processDefinitionKey);
                statisticForProcess.setName(process.getName());
                statisticForProcess.setBpmnProcessId(process.getBpmnProcessId());
                statisticForProcess.setVersion(process.getVersion());
                processStatistics.getProcesses().add(statisticForProcess);
            }
            processStatistics.recordInstancesCount(incidentsCount);
        }
        return processStatistics;
    }
}
