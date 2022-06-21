package io.camunda.operate.webapp.es.reader;

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
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.util.ElasticsearchUtil.QueryType;
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
import java.util.Collection;
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
import org.elasticsearch.index.query.ConstantScoreQueryBuilder;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
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
public class FlowNodeInstanceReader extends AbstractReader {
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

   public Map getFlowNodeInstances(FlowNodeInstanceRequestDto request) {
      Map response = new HashMap();
      Iterator var3 = request.getQueries().iterator();

      while(var3.hasNext()) {
         FlowNodeInstanceQueryDto query = (FlowNodeInstanceQueryDto)var3.next();
         response.put(query.getTreePath(), this.getFlowNodeInstances(query));
      }

      return response;
   }

   private FlowNodeInstanceResponseDto getFlowNodeInstances(FlowNodeInstanceQueryDto request) {
      FlowNodeInstanceResponseDto response = this.queryFlowNodeInstances(request);
      if (request.getSearchAfterOrEqual() != null || request.getSearchBeforeOrEqual() != null) {
         this.adjustResponse(response, request);
      }

      return response;
   }

   private void adjustResponse(FlowNodeInstanceResponseDto response, FlowNodeInstanceQueryDto request) {
      String flowNodeInstanceId = null;
      if (request.getSearchAfterOrEqual() != null) {
         flowNodeInstanceId = (String)request.getSearchAfterOrEqual()[1];
      } else if (request.getSearchBeforeOrEqual() != null) {
         flowNodeInstanceId = (String)request.getSearchBeforeOrEqual()[1];
      }

      FlowNodeInstanceQueryDto newRequest = request.createCopy().setSearchAfter((Object[])null).setSearchAfterOrEqual((Object[])null).setSearchBefore((Object[])null).setSearchBeforeOrEqual((Object[])null);
      List entities = this.queryFlowNodeInstances(newRequest, flowNodeInstanceId).getChildren();
      if (entities.size() > 0) {
         FlowNodeInstanceDto entity = (FlowNodeInstanceDto)entities.get(0);
         List children = response.getChildren();
         if (request.getSearchAfterOrEqual() != null) {
            if (request.getPageSize() != null && children.size() == request.getPageSize()) {
               children.remove(children.size() - 1);
            }

            children.add(0, entity);
         } else if (request.getSearchBeforeOrEqual() != null) {
            if (request.getPageSize() != null && children.size() == request.getPageSize()) {
               children.remove(0);
            }

            children.add(entity);
         }
      }

   }

   private FlowNodeInstanceResponseDto queryFlowNodeInstances(FlowNodeInstanceQueryDto flowNodeInstanceRequest) {
      return this.queryFlowNodeInstances(flowNodeInstanceRequest, (String)null);
   }

   private FlowNodeInstanceResponseDto queryFlowNodeInstances(FlowNodeInstanceQueryDto flowNodeInstanceRequest, String flowNodeInstanceId) {
      String parentTreePath = flowNodeInstanceRequest.getTreePath();
      int level = parentTreePath.split("/").length;
      IdsQueryBuilder idsQuery = null;
      if (flowNodeInstanceId != null) {
         idsQuery = QueryBuilders.idsQuery().addIds(new String[]{flowNodeInstanceId});
      }

      QueryBuilder query = QueryBuilders.constantScoreQuery(QueryBuilders.termQuery("processInstanceKey", flowNodeInstanceRequest.getProcessInstanceId()));
      AggregationBuilder incidentAgg = this.getIncidentsAgg();
      AggregationBuilder runningParentsAgg = AggregationBuilders.filter("running", ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("endDate")), QueryBuilders.termQuery("treePath", parentTreePath), QueryBuilders.termQuery("level", level - 1)}));
      QueryBuilder postFilter = ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{QueryBuilders.termQuery("level", level), QueryBuilders.termQuery("treePath", parentTreePath), idsQuery});
      SearchSourceBuilder searchSourceBuilder = (new SearchSourceBuilder()).query(query).aggregation(incidentAgg).aggregation(runningParentsAgg).postFilter(postFilter);
      if (flowNodeInstanceRequest.getPageSize() != null) {
         searchSourceBuilder.size(flowNodeInstanceRequest.getPageSize());
      }

      this.applySorting(searchSourceBuilder, flowNodeInstanceRequest);
      SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest(this.flowNodeInstanceTemplate).source(searchSourceBuilder);

      try {
         FlowNodeInstanceResponseDto response;
         if (flowNodeInstanceRequest.getPageSize() != null) {
            response = this.getOnePage(searchRequest);
         } else {
            response = this.scrollAllSearchHits(searchRequest);
         }

         if (level == 1) {
            response.setRunning((Boolean)null);
         }

         if (flowNodeInstanceRequest.getSearchBefore() != null || flowNodeInstanceRequest.getSearchBeforeOrEqual() != null) {
            Collections.reverse(response.getChildren());
         }

         return response;
      } catch (IOException var14) {
         String message = String.format("Exception occurred, while obtaining all flow node instances: %s", var14.getMessage());
         throw new OperateRuntimeException(message, var14);
      }
   }

   private AggregationBuilder getIncidentsAgg() {
      return AggregationBuilders.filter("incidents", QueryBuilders.termQuery("incident", true)).subAggregation(((TermsAggregationBuilder)AggregationBuilders.terms("aggIncidentPaths").field("treePath")).size(10000));
   }

   private FlowNodeInstanceResponseDto scrollAllSearchHits(SearchRequest searchRequest) throws IOException {
      Set incidentPaths = new HashSet();
      Boolean[] runningParent = new Boolean[]{false};
      List children = ElasticsearchUtil.scroll(searchRequest, FlowNodeInstanceEntity.class, this.objectMapper, this.esClient, this.getSearchHitFunction(incidentPaths), (Consumer)null, this.getAggsProcessor(incidentPaths, runningParent));
      return new FlowNodeInstanceResponseDto(runningParent[0], DtoCreator.create(children, FlowNodeInstanceDto.class));
   }

   private Function getSearchHitFunction(Set incidentPaths) {
      return (sh) -> {
         FlowNodeInstanceEntity entity = (FlowNodeInstanceEntity)ElasticsearchUtil.fromSearchHit(sh.getSourceAsString(), this.objectMapper, FlowNodeInstanceEntity.class);
         entity.setSortValues(sh.getSortValues());
         if (incidentPaths.contains(entity.getTreePath())) {
            entity.setIncident(true);
         }

         return entity;
      };
   }

   private FlowNodeInstanceResponseDto getOnePage(SearchRequest searchRequest) throws IOException {
      SearchResponse searchResponse = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
      Set incidentPaths = new HashSet();
      Boolean[] runningParent = new Boolean[1];
      this.processAggregation(searchResponse.getAggregations(), incidentPaths, runningParent);
      List children = ElasticsearchUtil.mapSearchHits(searchResponse.getHits().getHits(), this.getSearchHitFunction(incidentPaths));
      return new FlowNodeInstanceResponseDto(runningParent[0], DtoCreator.create(children, FlowNodeInstanceDto.class));
   }

   private Consumer getAggsProcessor(Set incidentPaths, Boolean[] runningParent) {
      return (aggs) -> {
         Filter filterAggs = (Filter)aggs.get("incidents");
         if (filterAggs != null) {
            Terms termsAggs = (Terms)filterAggs.getAggregations().get("aggIncidentPaths");
            if (termsAggs != null) {
               incidentPaths.addAll((Collection)termsAggs.getBuckets().stream().map((b) -> {
                  return b.getKeyAsString();
               }).collect(Collectors.toSet()));
            }
         }

         filterAggs = (Filter)aggs.get("running");
         if (filterAggs != null && filterAggs.getDocCount() > 0L) {
            runningParent[0] = true;
         }

      };
   }

   private Set processAggregation(Aggregations aggregations, Set incidentPaths, Boolean[] runningParent) {
      this.getAggsProcessor(incidentPaths, runningParent).accept(aggregations);
      return incidentPaths;
   }

   private void applySorting(SearchSourceBuilder searchSourceBuilder, FlowNodeInstanceQueryDto request) {
      boolean directSorting = request.getSearchAfter() != null || request.getSearchAfterOrEqual() != null || request.getSearchBefore() == null && request.getSearchBeforeOrEqual() == null;
      if (directSorting) {
         searchSourceBuilder.sort("startDate", SortOrder.ASC).sort("id", SortOrder.ASC);
         if (request.getSearchAfter() != null) {
            searchSourceBuilder.searchAfter(request.getSearchAfter());
         } else if (request.getSearchAfterOrEqual() != null) {
            searchSourceBuilder.searchAfter(request.getSearchAfterOrEqual());
         }
      } else {
         searchSourceBuilder.sort("startDate", SortOrder.DESC).sort("id", SortOrder.DESC);
         if (request.getSearchBefore() != null) {
            searchSourceBuilder.searchAfter(request.getSearchBefore());
         } else if (request.getSearchBeforeOrEqual() != null) {
            searchSourceBuilder.searchAfter(request.getSearchBeforeOrEqual());
         }
      }

   }

   public FlowNodeMetadataDto getFlowNodeMetadata(String processInstanceId, FlowNodeMetadataRequestDto request) {
      if (request.getFlowNodeId() != null) {
         return this.getMetadataByFlowNodeId(processInstanceId, request.getFlowNodeId(), request.getFlowNodeType());
      } else {
         return request.getFlowNodeInstanceId() != null ? this.getMetadataByFlowNodeInstanceId(request.getFlowNodeInstanceId()) : null;
      }
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
      String incidentTreePath = (new TreePath(treePath)).appendFlowNode(flowNodeId).appendFlowNodeInstance(flowNodeInstanceId).toString();
      QueryBuilder query = QueryBuilders.constantScoreQuery(ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{QueryBuilders.termQuery("treePath", incidentTreePath), IncidentTemplate.ACTIVE_INCIDENT_QUERY}));
      SearchRequest request = ElasticsearchUtil.createSearchRequest(this.incidentTemplate, QueryType.ONLY_RUNTIME).source((new SearchSourceBuilder()).query(query));

      try {
         SearchResponse response = this.esClient.search(request, RequestOptions.DEFAULT);
         flowNodeMetadata.setIncidentCount(response.getHits().getTotalHits().value);
         if (response.getHits().getTotalHits().value == 1L) {
            IncidentEntity incidentEntity = (IncidentEntity)ElasticsearchUtil.fromSearchHit(response.getHits().getAt(0).getSourceAsString(), this.objectMapper, IncidentEntity.class);
            Map incData = this.incidentReader.collectFlowNodeDataForPropagatedIncidents(List.of(incidentEntity), processInstanceId, treePath);
            DecisionInstanceReferenceDto rootCauseDecision = null;
            if (flowNodeType.equals(FlowNodeType.BUSINESS_RULE_TASK)) {
               rootCauseDecision = this.findRootCauseDecision(incidentEntity.getFlowNodeInstanceKey());
            }

            IncidentDto incidentDto = IncidentDto.createFrom(incidentEntity, Map.of(incidentEntity.getProcessDefinitionKey(), this.processCache.getProcessNameOrBpmnProcessId(incidentEntity.getProcessDefinitionKey(), "Unknown process")), (IncidentReader.IncidentDataHolder)incData.get(incidentEntity.getId()), rootCauseDecision);
            flowNodeMetadata.setIncident(incidentDto);
         }

      } catch (IOException var15) {
         String message = String.format("Exception occurred, while obtaining incidents: %s", var15.getMessage());
         throw new OperateRuntimeException(message, var15);
      }
   }

   private void searchForIncidentsByFlowNodeIdAndType(FlowNodeMetadataDto flowNodeMetadata, String processInstanceId, String flowNodeId, FlowNodeType flowNodeType) {
      String treePath = this.processInstanceReader.getProcessInstanceTreePath(processInstanceId);
      String flowNodeInstancesTreePath = (new TreePath(treePath)).appendFlowNode(flowNodeId).toString();
      QueryBuilder query = QueryBuilders.constantScoreQuery(ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{QueryBuilders.termQuery("treePath", flowNodeInstancesTreePath), IncidentTemplate.ACTIVE_INCIDENT_QUERY}));
      SearchRequest request = ElasticsearchUtil.createSearchRequest(this.incidentTemplate, QueryType.ONLY_RUNTIME).source((new SearchSourceBuilder()).query(query));

      try {
         SearchResponse response = this.esClient.search(request, RequestOptions.DEFAULT);
         flowNodeMetadata.setIncidentCount(response.getHits().getTotalHits().value);
         if (response.getHits().getTotalHits().value == 1L) {
            IncidentEntity incidentEntity = (IncidentEntity)ElasticsearchUtil.fromSearchHit(response.getHits().getAt(0).getSourceAsString(), this.objectMapper, IncidentEntity.class);
            Map incData = this.incidentReader.collectFlowNodeDataForPropagatedIncidents(List.of(incidentEntity), processInstanceId, treePath);
            DecisionInstanceReferenceDto rootCauseDecision = null;
            if (flowNodeType.equals(FlowNodeType.BUSINESS_RULE_TASK)) {
               rootCauseDecision = this.findRootCauseDecision(incidentEntity.getFlowNodeInstanceKey());
            }

            IncidentDto incidentDto = IncidentDto.createFrom(incidentEntity, Map.of(incidentEntity.getProcessDefinitionKey(), this.processCache.getProcessNameOrBpmnProcessId(incidentEntity.getProcessDefinitionKey(), "Unknown process")), (IncidentReader.IncidentDataHolder)incData.get(incidentEntity.getId()), rootCauseDecision);
            flowNodeMetadata.setIncident(incidentDto);
         }

      } catch (IOException var14) {
         String message = String.format("Exception occurred, while obtaining incidents: %s", var14.getMessage());
         throw new OperateRuntimeException(message, var14);
      }
   }

   private DecisionInstanceReferenceDto findRootCauseDecision(Long flowNodeInstanceKey) {
      SearchRequest request = ElasticsearchUtil.createSearchRequest(this.decisionInstanceTemplate).source((new SearchSourceBuilder()).query(ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{QueryBuilders.termQuery("elementInstanceKey", flowNodeInstanceKey), QueryBuilders.termQuery("state", DecisionInstanceState.FAILED)})).sort("evaluationDate", SortOrder.DESC).size(1).fetchSource(new String[]{"decisionName", "decisionId"}, (String[])null));

      try {
         SearchResponse response = this.esClient.search(request, RequestOptions.DEFAULT);
         if (response.getHits().getTotalHits().value > 0L) {
            Map source = response.getHits().getHits()[0].getSourceAsMap();
            String decisionName = (String)source.get("decisionName");
            if (decisionName == null) {
               decisionName = (String)source.get("decisionId");
            }

            return (new DecisionInstanceReferenceDto()).setDecisionName(decisionName).setInstanceId(response.getHits().getHits()[0].getId());
         } else {
            return null;
         }
      } catch (IOException var6) {
         String message = String.format("Exception occurred, while searching for root cause decision. Flow node instance id: %s. Error message: %s.", flowNodeInstanceKey, var6.getMessage());
         throw new OperateRuntimeException(message, var6);
      }
   }

   private FlowNodeInstanceEntity getFlowNodeInstanceEntity(String flowNodeInstanceId) {
      TermQueryBuilder flowNodeInstanceIdQ = QueryBuilders.termQuery("id", flowNodeInstanceId);
      SearchRequest request = ElasticsearchUtil.createSearchRequest(this.flowNodeInstanceTemplate).source((new SearchSourceBuilder()).query(QueryBuilders.constantScoreQuery(flowNodeInstanceIdQ)));

      try {
         SearchResponse response = this.esClient.search(request, RequestOptions.DEFAULT);
         FlowNodeInstanceEntity flowNodeInstance = this.getFlowNodeInstance(response);
         return flowNodeInstance;
      } catch (IOException var8) {
         String message = String.format("Exception occurred, while obtaining metadata for flow node instance: %s", var8.getMessage());
         throw new OperateRuntimeException(message, var8);
      }
   }

   private List buildBreadcrumb(String treePath, String flowNodeId, int level) {
      List result = new ArrayList();
      QueryBuilder query = ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{QueryBuilders.termQuery("flowNodeId", flowNodeId), QueryBuilders.matchQuery("treePath", treePath), QueryBuilders.rangeQuery("level").lte(level)});
      SearchRequest request = ElasticsearchUtil.createSearchRequest(this.flowNodeInstanceTemplate).source((new SearchSourceBuilder()).query(QueryBuilders.constantScoreQuery(query)).fetchSource(false).size(0).aggregation(this.getLevelsAggs()));

      try {
         SearchResponse response = this.esClient.search(request, RequestOptions.DEFAULT);
         Terms levelsAgg = (Terms)response.getAggregations().get("levelsAgg");
         result.addAll(this.buildBreadcrumbForFlowNodeId(levelsAgg.getBuckets(), level));
         return result;
      } catch (IOException var9) {
         String message = String.format("Exception occurred, while obtaining metadata for flow node: %s", var9.getMessage());
         throw new OperateRuntimeException(message, var9);
      }
   }

   private FlowNodeMetadataDto getMetadataByFlowNodeId(String processInstanceId, String flowNodeId, FlowNodeType flowNodeType) {
      TermQueryBuilder processInstanceIdQ = QueryBuilders.termQuery("processInstanceKey", processInstanceId);
      TermQueryBuilder flowNodeIdQ = QueryBuilders.termQuery("flowNodeId", flowNodeId);
      SearchSourceBuilder sourceBuilder = (new SearchSourceBuilder()).query(QueryBuilders.constantScoreQuery(ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{processInstanceIdQ, flowNodeIdQ}))).sort("level", SortOrder.ASC).aggregation(this.getLevelsAggs()).size(1);
      if (flowNodeType != null) {
         sourceBuilder.postFilter(QueryBuilders.termQuery("type", flowNodeType));
      }

      SearchRequest request = ElasticsearchUtil.createSearchRequest(this.flowNodeInstanceTemplate).source(sourceBuilder);

      try {
         SearchResponse response = this.esClient.search(request, RequestOptions.DEFAULT);
         FlowNodeMetadataDto result = new FlowNodeMetadataDto();
         FlowNodeInstanceEntity flowNodeInstance = this.getFlowNodeInstance(response);
         Terms levelsAgg = (Terms)response.getAggregations().get("levelsAgg");
         if (levelsAgg != null && levelsAgg.getBuckets() != null && levelsAgg.getBuckets().size() > 0) {
            Terms.Bucket bucketCurrentLevel = this.getBucketFromLevel(levelsAgg.getBuckets(), flowNodeInstance.getLevel());
            if (bucketCurrentLevel.getDocCount() == 1L) {
               result.setInstanceMetadata(this.buildInstanceMetadata(flowNodeInstance));
               result.setFlowNodeInstanceId(flowNodeInstance.getId());
               result.setBreadcrumb(this.buildBreadcrumbForFlowNodeId(levelsAgg.getBuckets(), flowNodeInstance.getLevel()));
               this.searchForIncidents(result, String.valueOf(flowNodeInstance.getProcessInstanceKey()), flowNodeInstance.getFlowNodeId(), flowNodeInstance.getId(), flowNodeInstance.getType());
            } else {
               result.setInstanceCount(bucketCurrentLevel.getDocCount());
               result.setFlowNodeId(flowNodeInstance.getFlowNodeId());
               result.setFlowNodeType(flowNodeInstance.getType());
               this.searchForIncidentsByFlowNodeIdAndType(result, String.valueOf(flowNodeInstance.getProcessInstanceKey()), flowNodeInstance.getFlowNodeId(), flowNodeInstance.getType());
            }
         }

         return result;
      } catch (IOException var13) {
         String message = String.format("Exception occurred, while obtaining metadata for flow node: %s", var13.getMessage());
         throw new OperateRuntimeException(message, var13);
      }
   }

   private Terms.Bucket getBucketFromLevel(List buckets, int level) {
      return (Terms.Bucket)buckets.stream().filter((b) -> {
         return b.getKeyAsNumber().intValue() == level;
      }).findFirst().get();
   }

   private TermsAggregationBuilder getLevelsAggs() {
      return (TermsAggregationBuilder)((TermsAggregationBuilder)AggregationBuilders.terms("levelsAgg").field("level")).size(10000).order(BucketOrder.key(true)).subAggregation(AggregationBuilders.topHits("levelsTopHitsAgg").size(1));
   }

   private FlowNodeInstanceEntity getFlowNodeInstance(SearchResponse response) {
      if (response.getHits().getTotalHits().value == 0L) {
         throw new OperateRuntimeException("No data found for flow node instance.");
      } else {
         return (FlowNodeInstanceEntity)ElasticsearchUtil.fromSearchHit(response.getHits().getAt(0).getSourceAsString(), this.objectMapper, FlowNodeInstanceEntity.class);
      }
   }

   private List buildBreadcrumbForFlowNodeId(List buckets, int currentInstanceLevel) {
      if (buckets.size() == 0) {
         return new ArrayList();
      } else {
         List breadcrumb = new ArrayList();
         FlowNodeType firstBucketFlowNodeType = this.getFirstBucketFlowNodeType(buckets);
         if (firstBucketFlowNodeType != null && firstBucketFlowNodeType.equals(FlowNodeType.MULTI_INSTANCE_BODY) || this.getBucketFromLevel(buckets, currentInstanceLevel).getDocCount() > 1L) {
            Iterator var5 = buckets.iterator();

            while(var5.hasNext()) {
               Terms.Bucket levelBucket = (Terms.Bucket)var5.next();
               TopHits levelTopHits = (TopHits)levelBucket.getAggregations().get("levelsTopHitsAgg");
               Map instanceFields = levelTopHits.getHits().getAt(0).getSourceAsMap();
               if ((Integer)instanceFields.get("level") <= currentInstanceLevel) {
                  breadcrumb.add(new FlowNodeInstanceBreadcrumbEntryDto((String)instanceFields.get("flowNodeId"), FlowNodeType.valueOf((String)instanceFields.get("type"))));
               }
            }
         }

         return breadcrumb;
      }
   }

   private FlowNodeType getFirstBucketFlowNodeType(List buckets) {
      TopHits topHits = (TopHits)((Terms.Bucket)buckets.get(0)).getAggregations().get("levelsTopHitsAgg");
      if (topHits != null && topHits.getHits().getTotalHits().value > 0L) {
         String type = (String)topHits.getHits().getAt(0).getSourceAsMap().get("type");
         if (type != null) {
            return FlowNodeType.valueOf(type);
         }
      }

      return null;
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
      } else {
         if (flowNodeInstance.getType().equals(FlowNodeType.CALL_ACTIVITY)) {
            this.findCalledProcessInstance(flowNodeInstance.getId(), (sh) -> {
               calledProcessInstanceId[0] = sh.getId();
               Map source = sh.getSourceAsMap();
               String processName = (String)source.get("processName");
               if (processName == null) {
                  processName = (String)source.get("bpmnProcessId");
               }

               calledProcessDefinitionName[0] = processName;
            });
         } else if (flowNodeInstance.getType().equals(FlowNodeType.BUSINESS_RULE_TASK)) {
            this.findCalledDecisionInstance(flowNodeInstance.getId(), (sh) -> {
               Map source = sh.getSourceAsMap();
               String rootDecisionDefId = (String)source.get("rootDecisionDefinitionId");
               String decisionDefId = (String)source.get("decisionDefinitionId");
               String decisionName;
               if (rootDecisionDefId.equals(decisionDefId)) {
                  calledDecisionInstanceId[0] = sh.getId();
                  decisionName = (String)source.get("decisionName");
                  if (decisionName == null) {
                     decisionName = (String)source.get("decisionId");
                  }

                  calledDecisionDefinitionName[0] = decisionName;
               } else {
                  decisionName = (String)source.get("rootDecisionName");
                  if (decisionName == null) {
                     decisionName = (String)source.get("rootDecisionId");
                  }

                  calledDecisionDefinitionName[0] = decisionName;
               }

            });
         }

         return FlowNodeInstanceMetadataDto.createFrom(flowNodeInstance, eventEntity, calledProcessInstanceId[0], calledProcessDefinitionName[0], calledDecisionInstanceId[0], calledDecisionDefinitionName[0]);
      }
   }

   private void findCalledProcessInstance(String flowNodeInstanceId, Consumer processInstanceConsumer) {
      TermQueryBuilder parentFlowNodeInstanceQ = QueryBuilders.termQuery("parentFlowNodeInstanceKey", flowNodeInstanceId);
      SearchRequest request = ElasticsearchUtil.createSearchRequest(this.listViewTemplate).source((new SearchSourceBuilder()).query(parentFlowNodeInstanceQ).fetchSource(new String[]{"processName", "bpmnProcessId"}, (String[])null));

      try {
         SearchResponse response = this.esClient.search(request, RequestOptions.DEFAULT);
         if (response.getHits().getTotalHits().value >= 1L) {
            processInstanceConsumer.accept(response.getHits().getAt(0));
         }

      } catch (IOException var7) {
         String message = String.format("Exception occurred, while obtaining parent process instance id for flow node instance: %s", var7.getMessage());
         throw new OperateRuntimeException(message, var7);
      }
   }

   private void findCalledDecisionInstance(String flowNodeInstanceId, Consumer decisionInstanceConsumer) {
      TermQueryBuilder flowNodeInstanceQ = QueryBuilders.termQuery("elementInstanceKey", flowNodeInstanceId);
      SearchRequest request = ElasticsearchUtil.createSearchRequest(this.decisionInstanceTemplate).source((new SearchSourceBuilder()).query(flowNodeInstanceQ).fetchSource(new String[]{"rootDecisionDefinitionId", "rootDecisionName", "rootDecisionId", "decisionDefinitionId", "decisionName", "decisionId"}, (String[])null).sort("evaluationDate", SortOrder.DESC).sort("executionIndex", SortOrder.DESC));

      try {
         SearchResponse response = this.esClient.search(request, RequestOptions.DEFAULT);
         if (response.getHits().getTotalHits().value >= 1L) {
            decisionInstanceConsumer.accept(response.getHits().getAt(0));
         }

      } catch (IOException var7) {
         String message = String.format("Exception occurred, while obtaining calles decision instance id for flow node instance: %s", var7.getMessage());
         throw new OperateRuntimeException(message, var7);
      }
   }

   private EventEntity getEventEntity(String flowNodeInstanceId) {
      QueryBuilder query = QueryBuilders.constantScoreQuery(QueryBuilders.termQuery("flowNodeInstanceKey", flowNodeInstanceId));
      SearchRequest request = ElasticsearchUtil.createSearchRequest(this.eventTemplate).source((new SearchSourceBuilder()).query(query).sort("id"));

      try {
         SearchResponse response = this.esClient.search(request, RequestOptions.DEFAULT);
         if (response.getHits().getTotalHits().value >= 1L) {
            EventEntity eventEntity = (EventEntity)ElasticsearchUtil.fromSearchHit(response.getHits().getHits()[(int)(response.getHits().getTotalHits().value - 1L)].getSourceAsString(), this.objectMapper, EventEntity.class);
            return eventEntity;
         } else {
            throw new NotFoundException(String.format("Could not find flow node instance event with id '%s'.", flowNodeInstanceId));
         }
      } catch (IOException var7) {
         String message = String.format("Exception occurred, while obtaining metadata for flow node instance: %s", var7.getMessage());
         throw new OperateRuntimeException(message, var7);
      }
   }

   public Map getFlowNodeStates(String processInstanceId) {
      String latestFlowNodeAggName = "latestFlowNode";
      String activeFlowNodesAggName = "activeFlowNodes";
      String activeFlowNodesBucketsAggName = "activeFlowNodesBuckets";
      String finishedFlowNodesAggName = "finishedFlowNodes";
      ConstantScoreQueryBuilder query = QueryBuilders.constantScoreQuery(QueryBuilders.termQuery("processInstanceKey", processInstanceId));
      AggregationBuilder notCompletedFlowNodesAggs = AggregationBuilders.filter("activeFlowNodes", QueryBuilders.termsQuery("state", new String[]{FlowNodeState.ACTIVE.name(), FlowNodeState.TERMINATED.name()})).subAggregation(((TermsAggregationBuilder)AggregationBuilders.terms("activeFlowNodesBuckets").field("flowNodeId")).size(10000).subAggregation(AggregationBuilders.topHits("latestFlowNode").sort("startDate", SortOrder.DESC).size(1).fetchSource(new String[]{"state", "treePath"}, (String[])null)));
      AggregationBuilder finishedFlowNodesAggs = AggregationBuilders.filter("finishedFlowNodes", QueryBuilders.termQuery("state", FlowNodeState.COMPLETED)).subAggregation(((TermsAggregationBuilder)AggregationBuilders.terms("finishedFlowNodesBuckets").field("flowNodeId")).size(10000));
      SearchRequest request = ElasticsearchUtil.createSearchRequest(this.flowNodeInstanceTemplate).source((new SearchSourceBuilder()).query(query).aggregation(notCompletedFlowNodesAggs).aggregation(this.getIncidentsAgg()).aggregation(finishedFlowNodesAggs).size(0));

      try {
         SearchResponse response = this.esClient.search(request, RequestOptions.DEFAULT);
         Set incidentPaths = new HashSet();
         this.processAggregation(response.getAggregations(), incidentPaths, new Boolean[]{false});
         Set finishedFlowNodes = this.collectFinishedFlowNodes((Filter)response.getAggregations().get("finishedFlowNodes"));
         Filter activeFlowNodesAgg = (Filter)response.getAggregations().get("activeFlowNodes");
         Terms flowNodesAgg = (Terms)activeFlowNodesAgg.getAggregations().get("activeFlowNodesBuckets");
         Map result = new HashMap();
         Iterator var16;
         Terms.Bucket flowNode;
         FlowNodeStateDto flowNodeState;
         if (flowNodesAgg != null) {
            for(var16 = flowNodesAgg.getBuckets().iterator(); var16.hasNext(); result.put(flowNode.getKeyAsString(), flowNodeState)) {
               flowNode = (Terms.Bucket)var16.next();
               Map lastFlowNodeFields = ((TopHits)flowNode.getAggregations().get("latestFlowNode")).getHits().getAt(0).getSourceAsMap();
               flowNodeState = FlowNodeStateDto.valueOf(lastFlowNodeFields.get("state").toString());
               if (flowNodeState.equals(FlowNodeStateDto.ACTIVE) && incidentPaths.contains(lastFlowNodeFields.get("treePath"))) {
                  flowNodeState = FlowNodeStateDto.INCIDENT;
               }
            }
         }

         var16 = finishedFlowNodes.iterator();

         while(var16.hasNext()) {
            String finishedFlowNodeId = (String)var16.next();
            if (result.get(finishedFlowNodeId) == null) {
               result.put(finishedFlowNodeId, FlowNodeStateDto.COMPLETED);
            }
         }

         return result;
      } catch (IOException var20) {
         String message = String.format("Exception occurred, while obtaining states for instance flow nodes: %s", var20.getMessage());
         throw new OperateRuntimeException(message, var20);
      }
   }

   private Set collectFinishedFlowNodes(Filter finishedFlowNodes) {
      Set result = new HashSet();
      List buckets = ((Terms)finishedFlowNodes.getAggregations().get("finishedFlowNodesBuckets")).getBuckets();
      if (buckets != null) {
         Iterator var4 = buckets.iterator();

         while(var4.hasNext()) {
            Terms.Bucket bucket = (Terms.Bucket)var4.next();
            result.add(bucket.getKeyAsString());
         }
      }

      return result;
   }
}
