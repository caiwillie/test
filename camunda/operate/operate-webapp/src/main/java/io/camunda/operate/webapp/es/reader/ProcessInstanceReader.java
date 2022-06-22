/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  io.camunda.operate.entities.listview.ProcessInstanceForListViewEntity
 *  io.camunda.operate.entities.listview.ProcessInstanceState
 *  io.camunda.operate.exceptions.OperateRuntimeException
 *  io.camunda.operate.schema.templates.IncidentTemplate
 *  io.camunda.operate.schema.templates.ListViewTemplate
 *  io.camunda.operate.schema.templates.TemplateDescriptor
 *  io.camunda.operate.util.ElasticsearchUtil
 *  io.camunda.operate.util.ElasticsearchUtil$QueryType
 *  io.camunda.operate.webapp.es.reader.AbstractReader
 *  io.camunda.operate.webapp.es.reader.OperationReader
 *  io.camunda.operate.webapp.rest.dto.ProcessInstanceCoreStatisticsDto
 *  io.camunda.operate.webapp.rest.dto.ProcessInstanceReferenceDto
 *  io.camunda.operate.webapp.rest.dto.listview.ListViewProcessInstanceDto
 *  io.camunda.operate.webapp.rest.exception.NotFoundException
 *  io.camunda.operate.zeebeimport.util.TreePath
 *  org.elasticsearch.action.search.SearchRequest
 *  org.elasticsearch.action.search.SearchResponse
 *  org.elasticsearch.client.RequestOptions
 *  org.elasticsearch.client.RestHighLevelClient
 *  org.elasticsearch.index.query.QueryBuilder
 *  org.elasticsearch.index.query.QueryBuilders
 *  org.elasticsearch.search.aggregations.AggregationBuilder
 *  org.elasticsearch.search.aggregations.AggregationBuilders
 *  org.elasticsearch.search.aggregations.Aggregations
 *  org.elasticsearch.search.aggregations.bucket.SingleBucketAggregation
 *  org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder
 *  org.elasticsearch.search.builder.SearchSourceBuilder
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.es.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.entities.listview.ProcessInstanceForListViewEntity;
import io.camunda.operate.entities.listview.ProcessInstanceState;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.schema.templates.IncidentTemplate;
import io.camunda.operate.schema.templates.ListViewTemplate;
import io.camunda.operate.schema.templates.TemplateDescriptor;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.webapp.es.reader.AbstractReader;
import io.camunda.operate.webapp.es.reader.OperationReader;
import io.camunda.operate.webapp.rest.dto.ProcessInstanceCoreStatisticsDto;
import io.camunda.operate.webapp.rest.dto.ProcessInstanceReferenceDto;
import io.camunda.operate.webapp.rest.dto.listview.ListViewProcessInstanceDto;
import io.camunda.operate.webapp.rest.exception.NotFoundException;
import io.camunda.operate.zeebeimport.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
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
public class ProcessInstanceReader
extends AbstractReader {
    private static final Logger logger = LoggerFactory.getLogger(ProcessInstanceReader.class);
    public static final FilterAggregationBuilder INCIDENTS_AGGREGATION = AggregationBuilders.filter((String)"incidents", (QueryBuilder)ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{QueryBuilders.termQuery((String)"incident", (boolean)true), QueryBuilders.termQuery((String)"joinRelation", (String)"processInstance")}));
    public static final FilterAggregationBuilder RUNNING_AGGREGATION = AggregationBuilders.filter((String)"running", (QueryBuilder)QueryBuilders.termQuery((String)"state", (Object)ProcessInstanceState.ACTIVE));
    @Autowired
    private ListViewTemplate listViewTemplate;
    @Autowired
    private IncidentTemplate incidentTemplate;
    @Autowired
    private OperationReader operationReader;

    public ListViewProcessInstanceDto getProcessInstanceWithOperationsByKey(Long processInstanceKey) {
        try {
            ProcessInstanceForListViewEntity processInstance = this.searchProcessInstanceByKey(processInstanceKey);
            List<ProcessInstanceReferenceDto> callHierarchy = this.createCallHierarchy(processInstance.getTreePath(), String.valueOf(processInstanceKey));
            return ListViewProcessInstanceDto.createFrom((ProcessInstanceForListViewEntity)processInstance, (List)this.operationReader.getOperationsByProcessInstanceKey(processInstanceKey), callHierarchy);
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining process instance with operations: %s", e.getMessage());
            logger.error(message, e);
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    private List<ProcessInstanceReferenceDto> createCallHierarchy(String treePath, String currentProcessInstanceId) {
        ArrayList<ProcessInstanceReferenceDto> callHierarchy = new ArrayList<ProcessInstanceReferenceDto>();
        List processInstanceIds = new TreePath(treePath).extractProcessInstanceIds();
        processInstanceIds.remove(currentProcessInstanceId);
        QueryBuilder query = ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{QueryBuilders.termQuery((String)"joinRelation", (String)"processInstance"), QueryBuilders.termsQuery((String)"id", (Collection)processInstanceIds)});
        SearchRequest request = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.listViewTemplate).source(new SearchSourceBuilder().query(query).fetchSource(new String[]{"id", "processDefinitionKey", "processName", "bpmnProcessId"}, null));
        try {
            ElasticsearchUtil.scrollWith((SearchRequest)request, (RestHighLevelClient)this.esClient, searchHits -> Arrays.stream(searchHits.getHits()).forEach(sh -> {
                Map source = sh.getSourceAsMap();
                callHierarchy.add(new ProcessInstanceReferenceDto().setInstanceId(String.valueOf(source.get("id"))).setProcessDefinitionId(String.valueOf(source.get("processDefinitionKey"))).setProcessDefinitionName(String.valueOf(source.getOrDefault("processName", source.get("bpmnProcessId")))));
            }));
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining process instance call hierarchy: %s", e.getMessage());
            throw new OperateRuntimeException(message, (Throwable)e);
        }
        callHierarchy.sort(Comparator.comparing(ref -> processInstanceIds.indexOf(ref.getInstanceId())));
        return callHierarchy;
    }

    public ProcessInstanceForListViewEntity getProcessInstanceByKey(Long processInstanceKey) {
        try {
            return this.searchProcessInstanceByKey(processInstanceKey);
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining process instance: %s", e.getMessage());
            logger.error(message, e);
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    protected ProcessInstanceForListViewEntity searchProcessInstanceByKey(Long processInstanceKey) throws IOException {
        QueryBuilder query = ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{QueryBuilders.idsQuery().addIds(new String[]{String.valueOf(processInstanceKey)}), QueryBuilders.termQuery((String)"processInstanceKey", (Object)processInstanceKey)});
        SearchRequest request = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.listViewTemplate, (ElasticsearchUtil.QueryType)ElasticsearchUtil.QueryType.ALL).source(new SearchSourceBuilder().query((QueryBuilder)QueryBuilders.constantScoreQuery((QueryBuilder)query)));
        SearchResponse response = this.esClient.search(request, RequestOptions.DEFAULT);
        if (response.getHits().getTotalHits().value == 1L) {
            ProcessInstanceForListViewEntity processInstance = (ProcessInstanceForListViewEntity)ElasticsearchUtil.fromSearchHit((String)response.getHits().getHits()[0].getSourceAsString(), (ObjectMapper)this.objectMapper, ProcessInstanceForListViewEntity.class);
            return processInstance;
        }
        if (response.getHits().getTotalHits().value <= 1L) throw new NotFoundException(String.format("Could not find process instance with id '%s'.", processInstanceKey));
        throw new NotFoundException(String.format("Could not find unique process instance with id '%s'.", processInstanceKey));
    }

    public ProcessInstanceCoreStatisticsDto getCoreStatistics() {
        SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.listViewTemplate, (ElasticsearchUtil.QueryType)ElasticsearchUtil.QueryType.ONLY_RUNTIME).source(new SearchSourceBuilder().size(0).aggregation((AggregationBuilder)INCIDENTS_AGGREGATION).aggregation((AggregationBuilder)RUNNING_AGGREGATION));
        try {
            SearchResponse response = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
            Aggregations aggregations = response.getAggregations();
            long runningCount = ((SingleBucketAggregation)aggregations.get("running")).getDocCount();
            long incidentCount = ((SingleBucketAggregation)aggregations.get("incidents")).getDocCount();
            ProcessInstanceCoreStatisticsDto processInstanceCoreStatisticsDto = new ProcessInstanceCoreStatisticsDto().setRunning(Long.valueOf(runningCount)).setActive(Long.valueOf(runningCount - incidentCount)).setWithIncidents(Long.valueOf(incidentCount));
            return processInstanceCoreStatisticsDto;
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining process instance core statistics: %s", e.getMessage());
            logger.error(message, e);
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    public String getProcessInstanceTreePath(String processInstanceId) {
        QueryBuilder query = ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{QueryBuilders.termQuery((String)"joinRelation", (String)"processInstance"), QueryBuilders.termQuery((String)"key", (String)processInstanceId)});
        SearchRequest request = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.listViewTemplate).source(new SearchSourceBuilder().query(query).fetchSource("treePath", null));
        try {
            SearchResponse response = this.esClient.search(request, RequestOptions.DEFAULT);
            if (response.getHits().getTotalHits().value <= 0L) throw new OperateRuntimeException(String.format("Process instance not found: %s", processInstanceId));
            return (String)response.getHits().getAt(0).getSourceAsMap().get("treePath");
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining tree path for process instance: %s", e.getMessage());
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }
}
