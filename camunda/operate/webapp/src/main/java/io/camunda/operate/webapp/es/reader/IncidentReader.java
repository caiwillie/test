/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  io.camunda.operate.cache.ProcessCache
 *  io.camunda.operate.entities.ErrorType
 *  io.camunda.operate.entities.IncidentEntity
 *  io.camunda.operate.exceptions.OperateRuntimeException
 *  io.camunda.operate.property.OperateProperties
 *  io.camunda.operate.schema.templates.FlowNodeInstanceTemplate
 *  io.camunda.operate.schema.templates.IncidentTemplate
 *  io.camunda.operate.schema.templates.TemplateDescriptor
 *  io.camunda.operate.util.CollectionUtil
 *  io.camunda.operate.util.ElasticsearchUtil
 *  io.camunda.operate.util.ElasticsearchUtil$QueryType
 *  io.camunda.operate.webapp.es.reader.AbstractReader
 *  io.camunda.operate.webapp.es.reader.IncidentReader$IncidentDataHolder
 *  io.camunda.operate.webapp.es.reader.OperationReader
 *  io.camunda.operate.webapp.es.reader.ProcessInstanceReader
 *  io.camunda.operate.webapp.rest.dto.incidents.IncidentDto
 *  io.camunda.operate.webapp.rest.dto.incidents.IncidentErrorTypeDto
 *  io.camunda.operate.webapp.rest.dto.incidents.IncidentFlowNodeDto
 *  io.camunda.operate.webapp.rest.dto.incidents.IncidentResponseDto
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
 *  org.elasticsearch.index.query.TermsQueryBuilder
 *  org.elasticsearch.search.SearchHit
 *  org.elasticsearch.search.aggregations.AggregationBuilder
 *  org.elasticsearch.search.aggregations.AggregationBuilders
 *  org.elasticsearch.search.aggregations.BucketOrder
 *  org.elasticsearch.search.aggregations.bucket.terms.Terms
 *  org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder
 *  org.elasticsearch.search.builder.SearchSourceBuilder
 *  org.elasticsearch.search.sort.SortOrder
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.es.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.cache.ProcessCache;
import io.camunda.operate.entities.ErrorType;
import io.camunda.operate.entities.IncidentEntity;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.schema.templates.FlowNodeInstanceTemplate;
import io.camunda.operate.schema.templates.IncidentTemplate;
import io.camunda.operate.schema.templates.TemplateDescriptor;
import io.camunda.operate.util.CollectionUtil;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.webapp.es.reader.AbstractReader;
import io.camunda.operate.webapp.es.reader.IncidentReader;
import io.camunda.operate.webapp.es.reader.OperationReader;
import io.camunda.operate.webapp.es.reader.ProcessInstanceReader;
import io.camunda.operate.webapp.rest.dto.incidents.IncidentDto;
import io.camunda.operate.webapp.rest.dto.incidents.IncidentErrorTypeDto;
import io.camunda.operate.webapp.rest.dto.incidents.IncidentFlowNodeDto;
import io.camunda.operate.webapp.rest.dto.incidents.IncidentResponseDto;
import io.camunda.operate.webapp.rest.exception.NotFoundException;
import io.camunda.operate.zeebeimport.util.TreePath;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IncidentReader
extends AbstractReader {
    private static final Logger logger = LoggerFactory.getLogger(IncidentReader.class);
    @Autowired
    private IncidentTemplate incidentTemplate;
    @Autowired
    private FlowNodeInstanceTemplate flowNodeInstanceTemplate;
    @Autowired
    private OperationReader operationReader;
    @Autowired
    private OperateProperties operateProperties;
    @Autowired
    private ProcessInstanceReader processInstanceReader;
    @Autowired
    private ProcessCache processCache;

    public List<IncidentEntity> getAllIncidentsByProcessInstanceKey(Long processInstanceKey) {
        TermQueryBuilder processInstanceKeyQuery = QueryBuilders.termQuery((String)"processInstanceKey", (Object)processInstanceKey);
        ConstantScoreQueryBuilder query = QueryBuilders.constantScoreQuery((QueryBuilder)ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{processInstanceKeyQuery, IncidentTemplate.ACTIVE_INCIDENT_QUERY}));
        SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.incidentTemplate, (ElasticsearchUtil.QueryType)ElasticsearchUtil.QueryType.ONLY_RUNTIME).source(new SearchSourceBuilder().query((QueryBuilder)query).sort("creationTime", SortOrder.ASC));
        try {
            return this.scroll(searchRequest, IncidentEntity.class);
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining all incidents: %s", e.getMessage());
            logger.error(message, e);
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    public Map<Long, List<Long>> getIncidentKeysPerProcessInstance(List<Long> processInstanceKeys) {
        ConstantScoreQueryBuilder processInstanceKeysQuery = QueryBuilders.constantScoreQuery((QueryBuilder)ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{QueryBuilders.termsQuery((String)"processInstanceKey", processInstanceKeys), IncidentTemplate.ACTIVE_INCIDENT_QUERY}));
        int batchSize = this.operateProperties.getElasticsearch().getBatchSize();
        SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.incidentTemplate, (ElasticsearchUtil.QueryType)ElasticsearchUtil.QueryType.ONLY_RUNTIME).source(new SearchSourceBuilder().query((QueryBuilder)processInstanceKeysQuery).fetchSource("processInstanceKey", null).size(batchSize));
        HashMap<Long, List<Long>> result = new HashMap<Long, List<Long>>();
        try {
            ElasticsearchUtil.scrollWith((SearchRequest)searchRequest, (RestHighLevelClient)this.esClient, searchHits -> {
                SearchHit[] searchHitArray = searchHits.getHits();
                int n = searchHitArray.length;
                int n2 = 0;
                while (n2 < n) {
                    SearchHit hit = searchHitArray[n2];
                    CollectionUtil.addToMap((Map)result, (Object)Long.valueOf(hit.getSourceAsMap().get("processInstanceKey").toString()), (Object)Long.valueOf(hit.getId()));
                    ++n2;
                }
            }, null, null);
            return result;
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining all incidents: %s", e.getMessage());
            logger.error(message, e);
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    public IncidentEntity getIncidentById(Long incidentKey) {
        IdsQueryBuilder idsQ = QueryBuilders.idsQuery().addIds(new String[]{incidentKey.toString()});
        ConstantScoreQueryBuilder query = QueryBuilders.constantScoreQuery((QueryBuilder)ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{idsQ, IncidentTemplate.ACTIVE_INCIDENT_QUERY}));
        SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.incidentTemplate, (ElasticsearchUtil.QueryType)ElasticsearchUtil.QueryType.ONLY_RUNTIME).source(new SearchSourceBuilder().query((QueryBuilder)query));
        try {
            SearchResponse response = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
            if (response.getHits().getTotalHits().value == 1L) {
                return (IncidentEntity)ElasticsearchUtil.fromSearchHit((String)response.getHits().getHits()[0].getSourceAsString(), (ObjectMapper)this.objectMapper, IncidentEntity.class);
            }
            if (response.getHits().getTotalHits().value <= 1L) throw new NotFoundException(String.format("Could not find incident with key '%s'.", incidentKey));
            throw new NotFoundException(String.format("Could not find unique incident with key '%s'.", incidentKey));
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining incident: %s", e.getMessage());
            logger.error(message, e);
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    public IncidentResponseDto getIncidentsByProcessInstanceId(String processInstanceId) {
        String treePath = this.processInstanceReader.getProcessInstanceTreePath(processInstanceId);
        TermQueryBuilder processInstanceQuery = QueryBuilders.termQuery((String)"treePath", (String)treePath);
        String errorTypesAggName = "errorTypesAgg";
        TermsAggregationBuilder errorTypesAgg = ((TermsAggregationBuilder)AggregationBuilders.terms((String)"errorTypesAgg").field("errorType")).size(ErrorType.values().length).order(BucketOrder.key((boolean)true));
        SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.incidentTemplate, (ElasticsearchUtil.QueryType)ElasticsearchUtil.QueryType.ONLY_RUNTIME).source(new SearchSourceBuilder().query((QueryBuilder)QueryBuilders.constantScoreQuery((QueryBuilder)ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{processInstanceQuery, IncidentTemplate.ACTIVE_INCIDENT_QUERY}))).aggregation((AggregationBuilder)errorTypesAgg));
        IncidentResponseDto incidentResponse = new IncidentResponseDto();
        HashMap processNames = new HashMap();
        try {
            List<IncidentEntity> incidents = this.scroll(searchRequest, IncidentEntity.class, aggs -> ((Terms)aggs.get("errorTypesAgg")).getBuckets().forEach(b -> {
                ErrorType errorType = ErrorType.valueOf((String)b.getKeyAsString());
                incidentResponse.getErrorTypes().add(IncidentErrorTypeDto.createFrom((ErrorType)errorType).setCount((int)b.getDocCount()));
            }));
            incidents.stream().filter(inc -> processNames.get(inc.getProcessDefinitionKey()) == null).forEach(inc -> processNames.put(inc.getProcessDefinitionKey(), this.processCache.getProcessNameOrBpmnProcessId(inc.getProcessDefinitionKey(), "Unknown process")));
            Map operations = this.operationReader.getOperationsPerIncidentKey(processInstanceId);
            Map<String, IncidentDataHolder> incData = this.collectFlowNodeDataForPropagatedIncidents(incidents, processInstanceId, treePath);
            incidentResponse.setFlowNodes(incData.values().stream().collect(Collectors.groupingBy(IncidentDataHolder::getFinalFlowNodeId, Collectors.counting())).entrySet().stream().map(entry -> new IncidentFlowNodeDto((String)entry.getKey(), ((Long)entry.getValue()).intValue())).collect(Collectors.toList()));
            List incidentsDtos = IncidentDto.sortDefault((List)IncidentDto.createFrom((List)incidents, (Map)operations, processNames, incData));
            incidentResponse.setIncidents(incidentsDtos);
            incidentResponse.setCount((long)incidents.size());
            return incidentResponse;
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining incidents: %s", e.getMessage());
            logger.error(message, e);
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    public Map<String, IncidentDataHolder> collectFlowNodeDataForPropagatedIncidents(List<IncidentEntity> incidents, String processInstanceId, String currentTreePath) {
        HashSet<String> flowNodeInstanceIdsSet = new HashSet<String>();
        HashMap<String, IncidentDataHolder> incDatas = new HashMap<String, IncidentDataHolder>();
        Iterator<IncidentEntity> iterator = incidents.iterator();
        while (true) {
            if (!iterator.hasNext()) {
                if (flowNodeInstanceIdsSet.size() <= 0) return incDatas;
                Map<String, String> flowNodeIdsMap = this.getFlowNodeIds(flowNodeInstanceIdsSet);
                incDatas.values().stream().filter(iData -> iData.getFinalFlowNodeId() == null).forEach(iData -> iData.setFinalFlowNodeId((String)flowNodeIdsMap.get(iData.getFinalFlowNodeInstanceId())));
                return incDatas;
            }
            IncidentEntity inc = iterator.next();
            IncidentDataHolder incData = new IncidentDataHolder().setIncidentId(inc.getId());
            if (!String.valueOf(inc.getProcessInstanceKey()).equals(processInstanceId)) {
                String callActivityInstanceId = TreePath.extractFlowNodeInstanceId((String)inc.getTreePath(), (String)currentTreePath);
                incData.setFinalFlowNodeInstanceId(callActivityInstanceId);
                flowNodeInstanceIdsSet.add(callActivityInstanceId);
            } else {
                incData.setFinalFlowNodeInstanceId(String.valueOf(inc.getFlowNodeInstanceKey()));
                incData.setFinalFlowNodeId(inc.getFlowNodeId());
            }
            incDatas.put(inc.getId(), incData);
        }
    }

    private Map<String, String> getFlowNodeIds(Set<String> flowNodeInstanceIds) {
        HashMap<String, String> flowNodeIdsMap = new HashMap<String, String>();
        TermsQueryBuilder q = QueryBuilders.termsQuery((String)"id", flowNodeInstanceIds);
        SearchRequest request = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.flowNodeInstanceTemplate, (ElasticsearchUtil.QueryType)ElasticsearchUtil.QueryType.ONLY_RUNTIME).source(new SearchSourceBuilder().query((QueryBuilder)q).fetchSource(new String[]{"id", "flowNodeId"}, null));
        try {
            ElasticsearchUtil.scrollWith((SearchRequest)request, (RestHighLevelClient)this.esClient, searchHits -> Arrays.stream(searchHits.getHits()).forEach(h -> flowNodeIdsMap.put(h.getId(), (String)h.getSourceAsMap().get("flowNodeId"))), null, null);
        }
        catch (IOException e) {
            throw new OperateRuntimeException("Exception occurred when searching for flow node ids: " + e.getMessage(), (Throwable)e);
        }
        return flowNodeIdsMap;
    }

    public class IncidentDataHolder {
        private String incidentId;
        private String finalFlowNodeInstanceId;
        private String finalFlowNodeId;

        public String getIncidentId() {
            return this.incidentId;
        }

        public IncidentDataHolder setIncidentId(String incidentId) {
            this.incidentId = incidentId;
            return this;
        }

        public String getFinalFlowNodeInstanceId() {
            return this.finalFlowNodeInstanceId;
        }

        public IncidentDataHolder setFinalFlowNodeInstanceId(String finalFlowNodeInstanceId) {
            this.finalFlowNodeInstanceId = finalFlowNodeInstanceId;
            return this;
        }

        public String getFinalFlowNodeId() {
            return this.finalFlowNodeId;
        }

        public IncidentDataHolder setFinalFlowNodeId(String finalFlowNodeId) {
            this.finalFlowNodeId = finalFlowNodeId;
            return this;
        }
    }
}
