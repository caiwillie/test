/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  io.camunda.operate.cache.ProcessCache
 *  io.camunda.operate.entities.EventEntity
 *  io.camunda.operate.entities.FlowNodeInstanceEntity
 *  io.camunda.operate.entities.FlowNodeState
 *  io.camunda.operate.entities.FlowNodeType
 *  io.camunda.operate.entities.IncidentEntity
 *  io.camunda.operate.entities.dmn.DecisionInstanceState
 *  io.camunda.operate.exceptions.OperateRuntimeException
 *  io.camunda.operate.schema.templates.DecisionInstanceTemplate
 *  io.camunda.operate.schema.templates.EventTemplate
 *  io.camunda.operate.schema.templates.FlowNodeInstanceTemplate
 *  io.camunda.operate.schema.templates.IncidentTemplate
 *  io.camunda.operate.schema.templates.ListViewTemplate
 *  io.camunda.operate.schema.templates.TemplateDescriptor
 *  io.camunda.operate.util.ElasticsearchUtil
 *  io.camunda.operate.util.ElasticsearchUtil$QueryType
 *  io.camunda.operate.webapp.es.reader.AbstractReader
 *  io.camunda.operate.webapp.es.reader.IncidentReader
 *  io.camunda.operate.webapp.es.reader.IncidentReader$IncidentDataHolder
 *  io.camunda.operate.webapp.es.reader.ProcessInstanceReader
 *  io.camunda.operate.webapp.rest.dto.DtoCreator
 *  io.camunda.operate.webapp.rest.dto.activity.FlowNodeInstanceDto
 *  io.camunda.operate.webapp.rest.dto.activity.FlowNodeInstanceQueryDto
 *  io.camunda.operate.webapp.rest.dto.activity.FlowNodeInstanceRequestDto
 *  io.camunda.operate.webapp.rest.dto.activity.FlowNodeInstanceResponseDto
 *  io.camunda.operate.webapp.rest.dto.activity.FlowNodeStateDto
 *  io.camunda.operate.webapp.rest.dto.incidents.IncidentDto
 *  io.camunda.operate.webapp.rest.dto.metadata.DecisionInstanceReferenceDto
 *  io.camunda.operate.webapp.rest.dto.metadata.FlowNodeInstanceBreadcrumbEntryDto
 *  io.camunda.operate.webapp.rest.dto.metadata.FlowNodeInstanceMetadataDto
 *  io.camunda.operate.webapp.rest.dto.metadata.FlowNodeMetadataDto
 *  io.camunda.operate.webapp.rest.dto.metadata.FlowNodeMetadataRequestDto
 *  io.camunda.operate.webapp.rest.exception.NotFoundException
 *  io.camunda.operate.zeebeimport.util.TreePath
 *  org.elasticsearch.action.search.SearchRequest
 *  org.elasticsearch.action.search.SearchResponse
 *  org.elasticsearch.client.RequestOptions
 *  org.elasticsearch.client.RestHighLevelClient
 *  org.elasticsearch.index.query.ConstantScoreQueryBuilder
 *  org.elasticsearch.index.query.IdsQueryBuilder
 *  org.elasticsearch.index.query.QueryBuilder
 *  org.elasticsearch.index.query.QueryBuilders
 *  org.elasticsearch.index.query.TermQueryBuilder
 *  org.elasticsearch.search.SearchHit
 *  org.elasticsearch.search.aggregations.AbstractAggregationBuilder
 *  org.elasticsearch.search.aggregations.AggregationBuilder
 *  org.elasticsearch.search.aggregations.AggregationBuilders
 *  org.elasticsearch.search.aggregations.Aggregations
 *  org.elasticsearch.search.aggregations.BucketOrder
 *  org.elasticsearch.search.aggregations.bucket.filter.Filter
 *  org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder
 *  org.elasticsearch.search.aggregations.bucket.terms.Terms
 *  org.elasticsearch.search.aggregations.bucket.terms.Terms$Bucket
 *  org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder
 *  org.elasticsearch.search.aggregations.metrics.TopHits
 *  org.elasticsearch.search.builder.SearchSourceBuilder
 *  org.elasticsearch.search.sort.SortOrder
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.es.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.cache.ProcessCache;
import io.camunda.operate.entities.EventEntity;
import io.camunda.operate.entities.FlowNodeInstanceEntity;
import io.camunda.operate.entities.FlowNodeState;
import io.camunda.operate.entities.FlowNodeType;
import io.camunda.operate.entities.IncidentEntity;
import io.camunda.operate.entities.dmn.DecisionInstanceState;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.schema.templates.DecisionInstanceTemplate;
import io.camunda.operate.schema.templates.EventTemplate;
import io.camunda.operate.schema.templates.FlowNodeInstanceTemplate;
import io.camunda.operate.schema.templates.IncidentTemplate;
import io.camunda.operate.schema.templates.ListViewTemplate;
import io.camunda.operate.schema.templates.TemplateDescriptor;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.webapp.es.reader.AbstractReader;
import io.camunda.operate.webapp.es.reader.IncidentReader;
import io.camunda.operate.webapp.es.reader.ProcessInstanceReader;
import io.camunda.operate.webapp.rest.dto.DtoCreator;
import io.camunda.operate.webapp.rest.dto.activity.FlowNodeInstanceDto;
import io.camunda.operate.webapp.rest.dto.activity.FlowNodeInstanceQueryDto;
import io.camunda.operate.webapp.rest.dto.activity.FlowNodeInstanceRequestDto;
import io.camunda.operate.webapp.rest.dto.activity.FlowNodeInstanceResponseDto;
import io.camunda.operate.webapp.rest.dto.activity.FlowNodeStateDto;
import io.camunda.operate.webapp.rest.dto.incidents.IncidentDto;
import io.camunda.operate.webapp.rest.dto.metadata.DecisionInstanceReferenceDto;
import io.camunda.operate.webapp.rest.dto.metadata.FlowNodeInstanceBreadcrumbEntryDto;
import io.camunda.operate.webapp.rest.dto.metadata.FlowNodeInstanceMetadataDto;
import io.camunda.operate.webapp.rest.dto.metadata.FlowNodeMetadataDto;
import io.camunda.operate.webapp.rest.dto.metadata.FlowNodeMetadataRequestDto;
import io.camunda.operate.webapp.rest.exception.NotFoundException;
import io.camunda.operate.zeebeimport.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.ConstantScoreQueryBuilder;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FlowNodeInstanceReader
extends AbstractReader {
    private static final Logger logger = LoggerFactory.getLogger(FlowNodeInstanceReader.class);
    public static final String AGG_INCIDENT_PATHS = "aggIncidentPaths";
    public static final String AGG_INCIDENTS = "incidents";
    public static final String AGG_RUNNING_PARENT = "running";
    public static final String LEVELS_AGG_NAME = "levelsAgg";
    public static final String LEVELS_TOP_HITS_AGG_NAME = "levelsTopHitsAgg";
    public static final String FINISHED_FLOW_NODES_BUCKETS_AGG_NAME = "finishedFlowNodesBuckets";
    @Autowired
    private FlowNodeInstanceTemplate flowNodeInstanceTemplate;
    @Autowired
    private EventTemplate eventTemplate;
    @Autowired
    private ListViewTemplate listViewTemplate;
    @Autowired
    private DecisionInstanceTemplate decisionInstanceTemplate;
    @Autowired
    private IncidentTemplate incidentTemplate;
    @Autowired
    private ProcessCache processCache;
    @Autowired
    private ProcessInstanceReader processInstanceReader;
    @Autowired
    private IncidentReader incidentReader;

    public Map<String, FlowNodeInstanceResponseDto> getFlowNodeInstances(FlowNodeInstanceRequestDto request) {
        HashMap<String, FlowNodeInstanceResponseDto> response = new HashMap<String, FlowNodeInstanceResponseDto>();
        Iterator iterator = request.getQueries().iterator();
        while (iterator.hasNext()) {
            FlowNodeInstanceQueryDto query = (FlowNodeInstanceQueryDto)iterator.next();
            response.put(query.getTreePath(), this.getFlowNodeInstances(query));
        }
        return response;
    }

    private FlowNodeInstanceResponseDto getFlowNodeInstances(FlowNodeInstanceQueryDto request) {
        FlowNodeInstanceResponseDto response = this.queryFlowNodeInstances(request);
        if (request.getSearchAfterOrEqual() == null) {
            if (request.getSearchBeforeOrEqual() == null) return response;
        }
        this.adjustResponse(response, request);
        return response;
    }

    private void adjustResponse(FlowNodeInstanceResponseDto response, FlowNodeInstanceQueryDto request) {
        String flowNodeInstanceId = null;
        if (request.getSearchAfterOrEqual() != null) {
            flowNodeInstanceId = (String)request.getSearchAfterOrEqual()[1];
        } else if (request.getSearchBeforeOrEqual() != null) {
            flowNodeInstanceId = (String)request.getSearchBeforeOrEqual()[1];
        }
        FlowNodeInstanceQueryDto newRequest = request.createCopy().setSearchAfter(null).setSearchAfterOrEqual(null).setSearchBefore(null).setSearchBeforeOrEqual(null);
        List entities = this.queryFlowNodeInstances(newRequest, flowNodeInstanceId).getChildren();
        if (entities.size() <= 0) return;
        FlowNodeInstanceDto entity = (FlowNodeInstanceDto)entities.get(0);
        List children = response.getChildren();
        if (request.getSearchAfterOrEqual() != null) {
            if (request.getPageSize() != null && children.size() == request.getPageSize().intValue()) {
                children.remove(children.size() - 1);
            }
            children.add(0, entity);
        } else {
            if (request.getSearchBeforeOrEqual() == null) return;
            if (request.getPageSize() != null && children.size() == request.getPageSize().intValue()) {
                children.remove(0);
            }
            children.add(entity);
        }
    }

    private FlowNodeInstanceResponseDto queryFlowNodeInstances(FlowNodeInstanceQueryDto flowNodeInstanceRequest) {
        return this.queryFlowNodeInstances(flowNodeInstanceRequest, null);
    }

    private FlowNodeInstanceResponseDto queryFlowNodeInstances(FlowNodeInstanceQueryDto flowNodeInstanceRequest, String flowNodeInstanceId) {
        String parentTreePath = flowNodeInstanceRequest.getTreePath();
        int level = parentTreePath.split("/").length;
        IdsQueryBuilder idsQuery = null;
        if (flowNodeInstanceId != null) {
            idsQuery = QueryBuilders.idsQuery().addIds(new String[]{flowNodeInstanceId});
        }
        ConstantScoreQueryBuilder query = QueryBuilders.constantScoreQuery((QueryBuilder)QueryBuilders.termQuery((String)"processInstanceKey", (String)flowNodeInstanceRequest.getProcessInstanceId()));
        AggregationBuilder incidentAgg = this.getIncidentsAgg();
        FilterAggregationBuilder runningParentsAgg = AggregationBuilders.filter((String)AGG_RUNNING_PARENT, (QueryBuilder)ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{QueryBuilders.boolQuery().mustNot((QueryBuilder)QueryBuilders.existsQuery((String)"endDate")), QueryBuilders.termQuery((String)"treePath", (String)parentTreePath), QueryBuilders.termQuery((String)"level", (int)(level - 1))}));
        QueryBuilder postFilter = ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{QueryBuilders.termQuery((String)"level", (int)level), QueryBuilders.termQuery((String)"treePath", (String)parentTreePath), idsQuery});
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query((QueryBuilder)query).aggregation(incidentAgg).aggregation((AggregationBuilder)runningParentsAgg).postFilter(postFilter);
        if (flowNodeInstanceRequest.getPageSize() != null) {
            searchSourceBuilder.size(flowNodeInstanceRequest.getPageSize().intValue());
        }
        this.applySorting(searchSourceBuilder, flowNodeInstanceRequest);
        SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.flowNodeInstanceTemplate).source(searchSourceBuilder);
        try {
            FlowNodeInstanceResponseDto response = flowNodeInstanceRequest.getPageSize() != null ? this.getOnePage(searchRequest) : this.scrollAllSearchHits(searchRequest);
            if (level == 1) {
                response.setRunning(null);
            }
            if (flowNodeInstanceRequest.getSearchBefore() == null) {
                if (flowNodeInstanceRequest.getSearchBeforeOrEqual() == null) return response;
            }
            Collections.reverse(response.getChildren());
            return response;
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining all flow node instances: %s", e.getMessage());
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    private AggregationBuilder getIncidentsAgg() {
        return AggregationBuilders.filter((String)AGG_INCIDENTS, (QueryBuilder)QueryBuilders.termQuery((String)"incident", (boolean)true)).subAggregation((AggregationBuilder)((TermsAggregationBuilder)AggregationBuilders.terms((String)AGG_INCIDENT_PATHS).field("treePath")).size(10000));
    }

    private FlowNodeInstanceResponseDto scrollAllSearchHits(SearchRequest searchRequest) throws IOException {
        HashSet<String> incidentPaths = new HashSet<String>();
        Boolean[] runningParent = new Boolean[]{false};
        List children = ElasticsearchUtil.scroll((SearchRequest)searchRequest, FlowNodeInstanceEntity.class, (ObjectMapper)this.objectMapper, (RestHighLevelClient)this.esClient, this.getSearchHitFunction(incidentPaths), null, this.getAggsProcessor(incidentPaths, runningParent));
        return new FlowNodeInstanceResponseDto(runningParent[0], DtoCreator.create((List)children, FlowNodeInstanceDto.class));
    }

    private Function<SearchHit, FlowNodeInstanceEntity> getSearchHitFunction(Set<String> incidentPaths) {
        return sh -> {
            FlowNodeInstanceEntity entity = (FlowNodeInstanceEntity)ElasticsearchUtil.fromSearchHit((String)sh.getSourceAsString(), (ObjectMapper)this.objectMapper, FlowNodeInstanceEntity.class);
            entity.setSortValues(sh.getSortValues());
            if (!incidentPaths.contains(entity.getTreePath())) return entity;
            entity.setIncident(true);
            return entity;
        };
    }

    private FlowNodeInstanceResponseDto getOnePage(SearchRequest searchRequest) throws IOException {
        SearchResponse searchResponse = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
        HashSet<String> incidentPaths = new HashSet<String>();
        Boolean[] runningParent = new Boolean[1];
        this.processAggregation(searchResponse.getAggregations(), incidentPaths, runningParent);
        List children = ElasticsearchUtil.mapSearchHits((SearchHit[])searchResponse.getHits().getHits(), this.getSearchHitFunction(incidentPaths));
        return new FlowNodeInstanceResponseDto(runningParent[0], DtoCreator.create((List)children, FlowNodeInstanceDto.class));
    }

    private Consumer<Aggregations> getAggsProcessor(Set<String> incidentPaths, Boolean[] runningParent) {
        return aggs -> {
            Terms termsAggs;
            Filter filterAggs = (Filter)aggs.get(AGG_INCIDENTS);
            if (filterAggs != null && (termsAggs = (Terms)filterAggs.getAggregations().get(AGG_INCIDENT_PATHS)) != null) {
                incidentPaths.addAll(termsAggs.getBuckets().stream().map(b -> b.getKeyAsString()).collect(Collectors.toSet()));
            }
            if ((filterAggs = (Filter)aggs.get(AGG_RUNNING_PARENT)) == null) return;
            if (filterAggs.getDocCount() <= 0L) return;
            runningParent[0] = true;
        };
    }

    private Set<String> processAggregation(Aggregations aggregations, Set<String> incidentPaths, Boolean[] runningParent) {
        this.getAggsProcessor(incidentPaths, runningParent).accept(aggregations);
        return incidentPaths;
    }

    private void applySorting(SearchSourceBuilder searchSourceBuilder, FlowNodeInstanceQueryDto request) {
        boolean directSorting;
        boolean bl = directSorting = request.getSearchAfter() != null || request.getSearchAfterOrEqual() != null || request.getSearchBefore() == null && request.getSearchBeforeOrEqual() == null;
        if (directSorting) {
            searchSourceBuilder.sort("startDate", SortOrder.ASC).sort("id", SortOrder.ASC);
            if (request.getSearchAfter() != null) {
                searchSourceBuilder.searchAfter(request.getSearchAfter());
            } else {
                if (request.getSearchAfterOrEqual() == null) return;
                searchSourceBuilder.searchAfter(request.getSearchAfterOrEqual());
            }
        } else {
            searchSourceBuilder.sort("startDate", SortOrder.DESC).sort("id", SortOrder.DESC);
            if (request.getSearchBefore() != null) {
                searchSourceBuilder.searchAfter(request.getSearchBefore());
            } else {
                if (request.getSearchBeforeOrEqual() == null) return;
                searchSourceBuilder.searchAfter(request.getSearchBeforeOrEqual());
            }
        }
    }

    public FlowNodeMetadataDto getFlowNodeMetadata(String processInstanceId, FlowNodeMetadataRequestDto request) {
        if (request.getFlowNodeId() != null) {
            return this.getMetadataByFlowNodeId(processInstanceId, request.getFlowNodeId(), request.getFlowNodeType());
        }
        if (request.getFlowNodeInstanceId() == null) return null;
        return this.getMetadataByFlowNodeInstanceId(request.getFlowNodeInstanceId());
    }

    private FlowNodeMetadataDto getMetadataByFlowNodeInstanceId(String flowNodeInstanceId) {
        FlowNodeInstanceEntity flowNodeInstance = this.getFlowNodeInstanceEntity(flowNodeInstanceId);
        FlowNodeMetadataDto result = new FlowNodeMetadataDto();
        result.setInstanceMetadata(this.buildInstanceMetadata(flowNodeInstance));
        result.setFlowNodeInstanceId(flowNodeInstanceId);
        result.setBreadcrumb(this.buildBreadcrumb(flowNodeInstance.getTreePath(), flowNodeInstance.getFlowNodeId(), flowNodeInstance.getLevel()));
        this.searchForIncidents(result, String.valueOf(flowNodeInstance.getProcessInstanceKey()), flowNodeInstance.getFlowNodeId(), flowNodeInstance.getId(), flowNodeInstance.getType());
        return result;
    }

    private void searchForIncidents(FlowNodeMetadataDto flowNodeMetadata, String processInstanceId, String flowNodeId, String flowNodeInstanceId, FlowNodeType flowNodeType) {
        String treePath = this.processInstanceReader.getProcessInstanceTreePath(processInstanceId);
        String incidentTreePath = new TreePath(treePath).appendFlowNode(flowNodeId).appendFlowNodeInstance(flowNodeInstanceId).toString();
        ConstantScoreQueryBuilder query = QueryBuilders.constantScoreQuery((QueryBuilder)ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{QueryBuilders.termQuery((String)"treePath", (String)incidentTreePath), IncidentTemplate.ACTIVE_INCIDENT_QUERY}));
        SearchRequest request = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.incidentTemplate, (ElasticsearchUtil.QueryType)ElasticsearchUtil.QueryType.ONLY_RUNTIME).source(new SearchSourceBuilder().query((QueryBuilder)query));
        try {
            SearchResponse response = this.esClient.search(request, RequestOptions.DEFAULT);
            flowNodeMetadata.setIncidentCount(Long.valueOf(response.getHits().getTotalHits().value));
            if (response.getHits().getTotalHits().value != 1L) return;
            IncidentEntity incidentEntity = (IncidentEntity)ElasticsearchUtil.fromSearchHit((String)response.getHits().getAt(0).getSourceAsString(), (ObjectMapper)this.objectMapper, IncidentEntity.class);
            Map incData = this.incidentReader.collectFlowNodeDataForPropagatedIncidents(List.of(incidentEntity), processInstanceId, treePath);
            DecisionInstanceReferenceDto rootCauseDecision = null;
            if (flowNodeType.equals((Object)FlowNodeType.BUSINESS_RULE_TASK)) {
                rootCauseDecision = this.findRootCauseDecision(incidentEntity.getFlowNodeInstanceKey());
            }
            IncidentDto incidentDto = IncidentDto.createFrom((IncidentEntity)incidentEntity, Map.of(incidentEntity.getProcessDefinitionKey(), this.processCache.getProcessNameOrBpmnProcessId(incidentEntity.getProcessDefinitionKey(), "Unknown process")), (IncidentReader.IncidentDataHolder)((IncidentReader.IncidentDataHolder)incData.get(incidentEntity.getId())), (DecisionInstanceReferenceDto)rootCauseDecision);
            flowNodeMetadata.setIncident(incidentDto);
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining incidents: %s", e.getMessage());
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    private void searchForIncidentsByFlowNodeIdAndType(FlowNodeMetadataDto flowNodeMetadata, String processInstanceId, String flowNodeId, FlowNodeType flowNodeType) {
        String treePath = this.processInstanceReader.getProcessInstanceTreePath(processInstanceId);
        String flowNodeInstancesTreePath = new TreePath(treePath).appendFlowNode(flowNodeId).toString();
        ConstantScoreQueryBuilder query = QueryBuilders.constantScoreQuery((QueryBuilder)ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{QueryBuilders.termQuery((String)"treePath", (String)flowNodeInstancesTreePath), IncidentTemplate.ACTIVE_INCIDENT_QUERY}));
        SearchRequest request = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.incidentTemplate, (ElasticsearchUtil.QueryType)ElasticsearchUtil.QueryType.ONLY_RUNTIME).source(new SearchSourceBuilder().query((QueryBuilder)query));
        try {
            SearchResponse response = this.esClient.search(request, RequestOptions.DEFAULT);
            flowNodeMetadata.setIncidentCount(Long.valueOf(response.getHits().getTotalHits().value));
            if (response.getHits().getTotalHits().value != 1L) return;
            IncidentEntity incidentEntity = (IncidentEntity)ElasticsearchUtil.fromSearchHit((String)response.getHits().getAt(0).getSourceAsString(), (ObjectMapper)this.objectMapper, IncidentEntity.class);
            Map incData = this.incidentReader.collectFlowNodeDataForPropagatedIncidents(List.of(incidentEntity), processInstanceId, treePath);
            DecisionInstanceReferenceDto rootCauseDecision = null;
            if (flowNodeType.equals((Object)FlowNodeType.BUSINESS_RULE_TASK)) {
                rootCauseDecision = this.findRootCauseDecision(incidentEntity.getFlowNodeInstanceKey());
            }
            IncidentDto incidentDto = IncidentDto.createFrom((IncidentEntity)incidentEntity, Map.of(incidentEntity.getProcessDefinitionKey(), this.processCache.getProcessNameOrBpmnProcessId(incidentEntity.getProcessDefinitionKey(), "Unknown process")), (IncidentReader.IncidentDataHolder)((IncidentReader.IncidentDataHolder)incData.get(incidentEntity.getId())), (DecisionInstanceReferenceDto)rootCauseDecision);
            flowNodeMetadata.setIncident(incidentDto);
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining incidents: %s", e.getMessage());
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    private DecisionInstanceReferenceDto findRootCauseDecision(Long flowNodeInstanceKey) {
        SearchRequest request = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.decisionInstanceTemplate).source(new SearchSourceBuilder().query(ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{QueryBuilders.termQuery((String)"elementInstanceKey", (Object)flowNodeInstanceKey), QueryBuilders.termQuery((String)"state", (Object)DecisionInstanceState.FAILED)})).sort("evaluationDate", SortOrder.DESC).size(1).fetchSource(new String[]{"decisionName", "decisionId"}, null));
        try {
            SearchResponse response = this.esClient.search(request, RequestOptions.DEFAULT);
            if (response.getHits().getTotalHits().value <= 0L) return null;
            Map source = response.getHits().getHits()[0].getSourceAsMap();
            String decisionName = (String)source.get("decisionName");
            if (decisionName != null) return new DecisionInstanceReferenceDto().setDecisionName(decisionName).setInstanceId(response.getHits().getHits()[0].getId());
            decisionName = (String)source.get("decisionId");
            return new DecisionInstanceReferenceDto().setDecisionName(decisionName).setInstanceId(response.getHits().getHits()[0].getId());
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while searching for root cause decision. Flow node instance id: %s. Error message: %s.", flowNodeInstanceKey, e.getMessage());
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    private FlowNodeInstanceEntity getFlowNodeInstanceEntity(String flowNodeInstanceId) {
        FlowNodeInstanceEntity flowNodeInstance;
        TermQueryBuilder flowNodeInstanceIdQ = QueryBuilders.termQuery((String)"id", (String)flowNodeInstanceId);
        SearchRequest request = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.flowNodeInstanceTemplate).source(new SearchSourceBuilder().query((QueryBuilder)QueryBuilders.constantScoreQuery((QueryBuilder)flowNodeInstanceIdQ)));
        try {
            SearchResponse response = this.esClient.search(request, RequestOptions.DEFAULT);
            flowNodeInstance = this.getFlowNodeInstance(response);
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining metadata for flow node instance: %s", e.getMessage());
            throw new OperateRuntimeException(message, (Throwable)e);
        }
        return flowNodeInstance;
    }

    private List<FlowNodeInstanceBreadcrumbEntryDto> buildBreadcrumb(String treePath, String flowNodeId, int level) {
        ArrayList<FlowNodeInstanceBreadcrumbEntryDto> result = new ArrayList<FlowNodeInstanceBreadcrumbEntryDto>();
        QueryBuilder query = ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{QueryBuilders.termQuery((String)"flowNodeId", (String)flowNodeId), QueryBuilders.matchQuery((String)"treePath", (Object)treePath), QueryBuilders.rangeQuery((String)"level").lte((Object)level)});
        SearchRequest request = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.flowNodeInstanceTemplate).source(new SearchSourceBuilder().query((QueryBuilder)QueryBuilders.constantScoreQuery((QueryBuilder)query)).fetchSource(false).size(0).aggregation((AggregationBuilder)this.getLevelsAggs()));
        try {
            SearchResponse response = this.esClient.search(request, RequestOptions.DEFAULT);
            Terms levelsAgg = (Terms)response.getAggregations().get(LEVELS_AGG_NAME);
            result.addAll(this.buildBreadcrumbForFlowNodeId(levelsAgg.getBuckets(), level));
            return result;
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining metadata for flow node: %s", e.getMessage());
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    private FlowNodeMetadataDto getMetadataByFlowNodeId(String processInstanceId, String flowNodeId, FlowNodeType flowNodeType) {
        TermQueryBuilder processInstanceIdQ = QueryBuilders.termQuery((String)"processInstanceKey", (String)processInstanceId);
        TermQueryBuilder flowNodeIdQ = QueryBuilders.termQuery((String)"flowNodeId", (String)flowNodeId);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().query((QueryBuilder)QueryBuilders.constantScoreQuery((QueryBuilder)ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{processInstanceIdQ, flowNodeIdQ}))).sort("level", SortOrder.ASC).aggregation((AggregationBuilder)this.getLevelsAggs()).size(1);
        if (flowNodeType != null) {
            sourceBuilder.postFilter((QueryBuilder)QueryBuilders.termQuery((String)"type", (Object)flowNodeType));
        }
        SearchRequest request = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.flowNodeInstanceTemplate).source(sourceBuilder);
        try {
            SearchResponse response = this.esClient.search(request, RequestOptions.DEFAULT);
            FlowNodeMetadataDto result = new FlowNodeMetadataDto();
            FlowNodeInstanceEntity flowNodeInstance = this.getFlowNodeInstance(response);
            Terms levelsAgg = (Terms)response.getAggregations().get(LEVELS_AGG_NAME);
            if (levelsAgg == null) return result;
            if (levelsAgg.getBuckets() == null) return result;
            if (levelsAgg.getBuckets().size() <= 0) return result;
            Terms.Bucket bucketCurrentLevel = this.getBucketFromLevel(levelsAgg.getBuckets(), flowNodeInstance.getLevel());
            if (bucketCurrentLevel.getDocCount() == 1L) {
                result.setInstanceMetadata(this.buildInstanceMetadata(flowNodeInstance));
                result.setFlowNodeInstanceId(flowNodeInstance.getId());
                result.setBreadcrumb(this.buildBreadcrumbForFlowNodeId(levelsAgg.getBuckets(), flowNodeInstance.getLevel()));
                this.searchForIncidents(result, String.valueOf(flowNodeInstance.getProcessInstanceKey()), flowNodeInstance.getFlowNodeId(), flowNodeInstance.getId(), flowNodeInstance.getType());
            } else {
                result.setInstanceCount(Long.valueOf(bucketCurrentLevel.getDocCount()));
                result.setFlowNodeId(flowNodeInstance.getFlowNodeId());
                result.setFlowNodeType(flowNodeInstance.getType());
                this.searchForIncidentsByFlowNodeIdAndType(result, String.valueOf(flowNodeInstance.getProcessInstanceKey()), flowNodeInstance.getFlowNodeId(), flowNodeInstance.getType());
            }
            return result;
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining metadata for flow node: %s", e.getMessage());
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    private Terms.Bucket getBucketFromLevel(List<? extends Terms.Bucket> buckets, int level) {
        return buckets.stream().filter(b -> b.getKeyAsNumber().intValue() == level).findFirst().get();
    }

    private TermsAggregationBuilder getLevelsAggs() {
        return (TermsAggregationBuilder)((TermsAggregationBuilder)AggregationBuilders.terms((String)LEVELS_AGG_NAME).field("level")).size(10000).order(BucketOrder.key((boolean)true)).subAggregation((AggregationBuilder)AggregationBuilders.topHits((String)LEVELS_TOP_HITS_AGG_NAME).size(1));
    }

    private FlowNodeInstanceEntity getFlowNodeInstance(SearchResponse response) {
        if (response.getHits().getTotalHits().value != 0L) return (FlowNodeInstanceEntity)ElasticsearchUtil.fromSearchHit((String)response.getHits().getAt(0).getSourceAsString(), (ObjectMapper)this.objectMapper, FlowNodeInstanceEntity.class);
        throw new OperateRuntimeException("No data found for flow node instance.");
    }

    private List<FlowNodeInstanceBreadcrumbEntryDto> buildBreadcrumbForFlowNodeId(List<? extends Terms.Bucket> buckets, int currentInstanceLevel) {
        if (buckets.size() == 0) {
            return new ArrayList<FlowNodeInstanceBreadcrumbEntryDto>();
        }
        ArrayList<FlowNodeInstanceBreadcrumbEntryDto> breadcrumb = new ArrayList<FlowNodeInstanceBreadcrumbEntryDto>();
        FlowNodeType firstBucketFlowNodeType = this.getFirstBucketFlowNodeType(buckets);
        if (firstBucketFlowNodeType == null || !firstBucketFlowNodeType.equals((Object)FlowNodeType.MULTI_INSTANCE_BODY)) {
            if (this.getBucketFromLevel(buckets, currentInstanceLevel).getDocCount() <= 1L) return breadcrumb;
        }
        Iterator<? extends Terms.Bucket> iterator = buckets.iterator();
        while (iterator.hasNext()) {
            Terms.Bucket levelBucket = iterator.next();
            TopHits levelTopHits = (TopHits)levelBucket.getAggregations().get(LEVELS_TOP_HITS_AGG_NAME);
            Map instanceFields = levelTopHits.getHits().getAt(0).getSourceAsMap();
            if ((Integer)instanceFields.get("level") > currentInstanceLevel) continue;
            breadcrumb.add(new FlowNodeInstanceBreadcrumbEntryDto((String)instanceFields.get("flowNodeId"), FlowNodeType.valueOf((String)((String)instanceFields.get("type")))));
        }
        return breadcrumb;
    }

    private FlowNodeType getFirstBucketFlowNodeType(List<? extends Terms.Bucket> buckets) {
        TopHits topHits = (TopHits)buckets.get(0).getAggregations().get(LEVELS_TOP_HITS_AGG_NAME);
        if (topHits == null) return null;
        if (topHits.getHits().getTotalHits().value <= 0L) return null;
        String type = (String)topHits.getHits().getAt(0).getSourceAsMap().get("type");
        if (type == null) return null;
        return FlowNodeType.valueOf((String)type);
    }

    private FlowNodeInstanceMetadataDto buildInstanceMetadata(FlowNodeInstanceEntity flowNodeInstance) {
        EventEntity eventEntity = this.getEventEntity(flowNodeInstance.getId());
        String[] calledProcessInstanceId = new String[]{null};
        String[] calledProcessDefinitionName = new String[]{null};
        String[] calledDecisionInstanceId = new String[]{null};
        String[] calledDecisionDefinitionName = new String[]{null};
        FlowNodeType type = flowNodeInstance.getType();
        if (type == null) {
            logger.error(String.format("FlowNodeType for FlowNodeInstance with id %s is null", flowNodeInstance.getId()));
            return null;
        }
        if (flowNodeInstance.getType().equals((Object)FlowNodeType.CALL_ACTIVITY)) {
            this.findCalledProcessInstance(flowNodeInstance.getId(), sh -> {
                calledProcessInstanceId[0] = sh.getId();
                Map source = sh.getSourceAsMap();
                String processName = (String)source.get("processName");
                if (processName == null) {
                    processName = (String)source.get("bpmnProcessId");
                }
                calledProcessDefinitionName[0] = processName;
            });
        } else {
            if (!flowNodeInstance.getType().equals((Object)FlowNodeType.BUSINESS_RULE_TASK)) return FlowNodeInstanceMetadataDto.createFrom((FlowNodeInstanceEntity)flowNodeInstance, (EventEntity)eventEntity, (String)calledProcessInstanceId[0], (String)calledProcessDefinitionName[0], (String)calledDecisionInstanceId[0], (String)calledDecisionDefinitionName[0]);
            this.findCalledDecisionInstance(flowNodeInstance.getId(), sh -> {
                String decisionDefId;
                Map source = sh.getSourceAsMap();
                String rootDecisionDefId = (String)source.get("rootDecisionDefinitionId");
                if (rootDecisionDefId.equals(decisionDefId = (String)source.get("decisionDefinitionId"))) {
                    calledDecisionInstanceId[0] = sh.getId();
                    String decisionName = (String)source.get("decisionName");
                    if (decisionName == null) {
                        decisionName = (String)source.get("decisionId");
                    }
                    calledDecisionDefinitionName[0] = decisionName;
                } else {
                    String decisionName = (String)source.get("rootDecisionName");
                    if (decisionName == null) {
                        decisionName = (String)source.get("rootDecisionId");
                    }
                    calledDecisionDefinitionName[0] = decisionName;
                }
            });
        }
        return FlowNodeInstanceMetadataDto.createFrom((FlowNodeInstanceEntity)flowNodeInstance, (EventEntity)eventEntity, (String)calledProcessInstanceId[0], (String)calledProcessDefinitionName[0], (String)calledDecisionInstanceId[0], (String)calledDecisionDefinitionName[0]);
    }

    private void findCalledProcessInstance(String flowNodeInstanceId, Consumer<SearchHit> processInstanceConsumer) {
        TermQueryBuilder parentFlowNodeInstanceQ = QueryBuilders.termQuery((String)"parentFlowNodeInstanceKey", (String)flowNodeInstanceId);
        SearchRequest request = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.listViewTemplate).source(new SearchSourceBuilder().query((QueryBuilder)parentFlowNodeInstanceQ).fetchSource(new String[]{"processName", "bpmnProcessId"}, null));
        try {
            SearchResponse response = this.esClient.search(request, RequestOptions.DEFAULT);
            if (response.getHits().getTotalHits().value < 1L) return;
            processInstanceConsumer.accept(response.getHits().getAt(0));
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining parent process instance id for flow node instance: %s", e.getMessage());
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    private void findCalledDecisionInstance(String flowNodeInstanceId, Consumer<SearchHit> decisionInstanceConsumer) {
        TermQueryBuilder flowNodeInstanceQ = QueryBuilders.termQuery((String)"elementInstanceKey", (String)flowNodeInstanceId);
        SearchRequest request = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.decisionInstanceTemplate).source(new SearchSourceBuilder().query((QueryBuilder)flowNodeInstanceQ).fetchSource(new String[]{"rootDecisionDefinitionId", "rootDecisionName", "rootDecisionId", "decisionDefinitionId", "decisionName", "decisionId"}, null).sort("evaluationDate", SortOrder.DESC).sort("executionIndex", SortOrder.DESC));
        try {
            SearchResponse response = this.esClient.search(request, RequestOptions.DEFAULT);
            if (response.getHits().getTotalHits().value < 1L) return;
            decisionInstanceConsumer.accept(response.getHits().getAt(0));
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining calles decision instance id for flow node instance: %s", e.getMessage());
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    private EventEntity getEventEntity(String flowNodeInstanceId) {
        EventEntity eventEntity;
        ConstantScoreQueryBuilder query = QueryBuilders.constantScoreQuery((QueryBuilder)QueryBuilders.termQuery((String)"flowNodeInstanceKey", (String)flowNodeInstanceId));
        SearchRequest request = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.eventTemplate).source(new SearchSourceBuilder().query((QueryBuilder)query).sort("id"));
        try {
            SearchResponse response = this.esClient.search(request, RequestOptions.DEFAULT);
            if (response.getHits().getTotalHits().value < 1L) {
                throw new NotFoundException(String.format("Could not find flow node instance event with id '%s'.", flowNodeInstanceId));
            }
            eventEntity = (EventEntity)ElasticsearchUtil.fromSearchHit((String)response.getHits().getHits()[(int)(response.getHits().getTotalHits().value - 1L)].getSourceAsString(), (ObjectMapper)this.objectMapper, EventEntity.class);
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining metadata for flow node instance: %s", e.getMessage());
            throw new OperateRuntimeException(message, (Throwable)e);
        }
        return eventEntity;
    }

    public Map<String, FlowNodeStateDto> getFlowNodeStates(String processInstanceId) {
        String latestFlowNodeAggName = "latestFlowNode";
        String activeFlowNodesAggName = "activeFlowNodes";
        String activeFlowNodesBucketsAggName = "activeFlowNodesBuckets";
        String finishedFlowNodesAggName = "finishedFlowNodes";
        ConstantScoreQueryBuilder query = QueryBuilders.constantScoreQuery((QueryBuilder)QueryBuilders.termQuery((String)"processInstanceKey", (String)processInstanceId));
        AbstractAggregationBuilder notCompletedFlowNodesAggs = AggregationBuilders.filter((String)"activeFlowNodes", (QueryBuilder)QueryBuilders.termsQuery((String)"state", (String[])new String[]{FlowNodeState.ACTIVE.name(), FlowNodeState.TERMINATED.name()})).subAggregation((AggregationBuilder)((TermsAggregationBuilder)AggregationBuilders.terms((String)"activeFlowNodesBuckets").field("flowNodeId")).size(10000).subAggregation((AggregationBuilder)AggregationBuilders.topHits((String)"latestFlowNode").sort("startDate", SortOrder.DESC).size(1).fetchSource(new String[]{"state", "treePath"}, null)));
        AbstractAggregationBuilder finishedFlowNodesAggs = AggregationBuilders.filter((String)"finishedFlowNodes", (QueryBuilder)QueryBuilders.termQuery((String)"state", (Object)FlowNodeState.COMPLETED)).subAggregation((AggregationBuilder)((TermsAggregationBuilder)AggregationBuilders.terms((String)FINISHED_FLOW_NODES_BUCKETS_AGG_NAME).field("flowNodeId")).size(10000));
        SearchRequest request = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.flowNodeInstanceTemplate).source(new SearchSourceBuilder().query((QueryBuilder)query).aggregation((AggregationBuilder)notCompletedFlowNodesAggs).aggregation(this.getIncidentsAgg()).aggregation((AggregationBuilder)finishedFlowNodesAggs).size(0));
        try {
            SearchResponse response = this.esClient.search(request, RequestOptions.DEFAULT);
            HashSet<String> incidentPaths = new HashSet<String>();
            this.processAggregation(response.getAggregations(), incidentPaths, new Boolean[]{false});
            Set<String> finishedFlowNodes = this.collectFinishedFlowNodes((Filter)response.getAggregations().get("finishedFlowNodes"));
            Filter activeFlowNodesAgg = (Filter)response.getAggregations().get("activeFlowNodes");
            Terms flowNodesAgg = (Terms)activeFlowNodesAgg.getAggregations().get("activeFlowNodesBuckets");
            HashMap<String, FlowNodeStateDto> result = new HashMap<String, FlowNodeStateDto>();
            if (flowNodesAgg != null) {
                for (Terms.Bucket flowNode : flowNodesAgg.getBuckets()) {
                    Map lastFlowNodeFields = ((TopHits)flowNode.getAggregations().get("latestFlowNode")).getHits().getAt(0).getSourceAsMap();
                    FlowNodeStateDto flowNodeState = FlowNodeStateDto.valueOf((String)lastFlowNodeFields.get("state").toString());
                    if (flowNodeState.equals((Object)FlowNodeStateDto.ACTIVE) && incidentPaths.contains(lastFlowNodeFields.get("treePath"))) {
                        flowNodeState = FlowNodeStateDto.INCIDENT;
                    }
                    result.put(flowNode.getKeyAsString(), flowNodeState);
                }
            }
            Iterator<String> iterator = finishedFlowNodes.iterator();
            while (iterator.hasNext()) {
                String finishedFlowNodeId = iterator.next();
                if (result.get(finishedFlowNodeId) != null) continue;
                result.put(finishedFlowNodeId, FlowNodeStateDto.COMPLETED);
            }
            return result;
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining states for instance flow nodes: %s", e.getMessage());
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    private Set<String> collectFinishedFlowNodes(Filter finishedFlowNodes) {
        HashSet<String> result = new HashSet<String>();
        List buckets = ((Terms)finishedFlowNodes.getAggregations().get(FINISHED_FLOW_NODES_BUCKETS_AGG_NAME)).getBuckets();
        if (buckets == null) return result;
        Iterator iterator = buckets.iterator();
        while (iterator.hasNext()) {
            Terms.Bucket bucket = (Terms.Bucket)iterator.next();
            result.add(bucket.getKeyAsString());
        }
        return result;
    }
}
