package io.camunda.operate.webapp.es.reader;

import io.camunda.operate.cache.ProcessCache;
import io.camunda.operate.entities.ErrorType;
import io.camunda.operate.entities.IncidentEntity;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.schema.templates.FlowNodeInstanceTemplate;
import io.camunda.operate.schema.templates.IncidentTemplate;
import io.camunda.operate.util.CollectionUtil;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.util.ElasticsearchUtil.QueryType;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.ConstantScoreQueryBuilder;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
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
public class IncidentReader extends AbstractReader {
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

   public List getAllIncidentsByProcessInstanceKey(Long processInstanceKey) {
      TermQueryBuilder processInstanceKeyQuery = QueryBuilders.termQuery("processInstanceKey", processInstanceKey);
      ConstantScoreQueryBuilder query = QueryBuilders.constantScoreQuery(ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{processInstanceKeyQuery, IncidentTemplate.ACTIVE_INCIDENT_QUERY}));
      SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest(this.incidentTemplate, QueryType.ONLY_RUNTIME).source((new SearchSourceBuilder()).query(query).sort("creationTime", SortOrder.ASC));

      try {
         return this.scroll(searchRequest, IncidentEntity.class);
      } catch (IOException var7) {
         String message = String.format("Exception occurred, while obtaining all incidents: %s", var7.getMessage());
         logger.error(message, var7);
         throw new OperateRuntimeException(message, var7);
      }
   }

   public Map getIncidentKeysPerProcessInstance(List processInstanceKeys) {
      QueryBuilder processInstanceKeysQuery = QueryBuilders.constantScoreQuery(ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{QueryBuilders.termsQuery("processInstanceKey", processInstanceKeys), IncidentTemplate.ACTIVE_INCIDENT_QUERY}));
      int batchSize = this.operateProperties.getElasticsearch().getBatchSize();
      SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest(this.incidentTemplate, QueryType.ONLY_RUNTIME).source((new SearchSourceBuilder()).query(processInstanceKeysQuery).fetchSource("processInstanceKey", (String)null).size(batchSize));
      Map result = new HashMap();

      try {
         ElasticsearchUtil.scrollWith(searchRequest, this.esClient, (searchHits) -> {
            SearchHit[] var2 = searchHits.getHits();
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
               SearchHit hit = var2[var4];
               CollectionUtil.addToMap(result, Long.valueOf(hit.getSourceAsMap().get("processInstanceKey").toString()), Long.valueOf(hit.getId()));
            }

         }, (Consumer)null, (Consumer)null);
         return result;
      } catch (IOException var8) {
         String message = String.format("Exception occurred, while obtaining all incidents: %s", var8.getMessage());
         logger.error(message, var8);
         throw new OperateRuntimeException(message, var8);
      }
   }

   public IncidentEntity getIncidentById(Long incidentKey) {
      IdsQueryBuilder idsQ = QueryBuilders.idsQuery().addIds(new String[]{incidentKey.toString()});
      ConstantScoreQueryBuilder query = QueryBuilders.constantScoreQuery(ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{idsQ, IncidentTemplate.ACTIVE_INCIDENT_QUERY}));
      SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest(this.incidentTemplate, QueryType.ONLY_RUNTIME).source((new SearchSourceBuilder()).query(query));

      try {
         SearchResponse response = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
         if (response.getHits().getTotalHits().value == 1L) {
            return (IncidentEntity)ElasticsearchUtil.fromSearchHit(response.getHits().getHits()[0].getSourceAsString(), this.objectMapper, IncidentEntity.class);
         } else if (response.getHits().getTotalHits().value > 1L) {
            throw new NotFoundException(String.format("Could not find unique incident with key '%s'.", incidentKey));
         } else {
            throw new NotFoundException(String.format("Could not find incident with key '%s'.", incidentKey));
         }
      } catch (IOException var7) {
         String message = String.format("Exception occurred, while obtaining incident: %s", var7.getMessage());
         logger.error(message, var7);
         throw new OperateRuntimeException(message, var7);
      }
   }

   public IncidentResponseDto getIncidentsByProcessInstanceId(String processInstanceId) {
      String treePath = this.processInstanceReader.getProcessInstanceTreePath(processInstanceId);
      TermQueryBuilder processInstanceQuery = QueryBuilders.termQuery("treePath", treePath);
      String errorTypesAggName = "errorTypesAgg";
      TermsAggregationBuilder errorTypesAgg = ((TermsAggregationBuilder)AggregationBuilders.terms("errorTypesAgg").field("errorType")).size(ErrorType.values().length).order(BucketOrder.key(true));
      SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest(this.incidentTemplate, QueryType.ONLY_RUNTIME).source((new SearchSourceBuilder()).query(QueryBuilders.constantScoreQuery(ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{processInstanceQuery, IncidentTemplate.ACTIVE_INCIDENT_QUERY}))).aggregation(errorTypesAgg));
      IncidentResponseDto incidentResponse = new IncidentResponseDto();
      Map processNames = new HashMap();

      try {
         List<IncidentEntity> incidents = this.scroll(searchRequest, IncidentEntity.class, (aggs) -> {
            ((Terms)aggs.get("errorTypesAgg")).getBuckets().forEach((b) -> {
               ErrorType errorType = ErrorType.valueOf(b.getKeyAsString());
               incidentResponse.getErrorTypes().add(IncidentErrorTypeDto.createFrom(errorType).setCount((int)b.getDocCount()));
            });
         });
         incidents.stream().filter((inc) -> {
            return processNames.get(inc.getProcessDefinitionKey()) == null;
         }).forEach((inc) -> {
            processNames.put(inc.getProcessDefinitionKey(), this.processCache.getProcessNameOrBpmnProcessId(inc.getProcessDefinitionKey(), "Unknown process"));
         });
         Map operations = this.operationReader.getOperationsPerIncidentKey(processInstanceId);
         Map incData = this.collectFlowNodeDataForPropagatedIncidents(incidents, processInstanceId, treePath);
         incidentResponse.setFlowNodes((List)((Map)incData.values().stream().collect(Collectors.groupingBy(IncidentDataHolder::getFinalFlowNodeId, Collectors.counting()))).entrySet().stream().map((entry) -> {
            return new IncidentFlowNodeDto((String)entry.getKey(), ((Long)entry.getValue()).intValue());
         }).collect(Collectors.toList()));
         List incidentsDtos = IncidentDto.sortDefault(IncidentDto.createFrom((List)incidents, operations, (Map)processNames, (Map)incData));
         incidentResponse.setIncidents(incidentsDtos);
         incidentResponse.setCount((long)incidents.size());
         return incidentResponse;
      } catch (IOException var13) {
         String message = String.format("Exception occurred, while obtaining incidents: %s", var13.getMessage());
         logger.error(message, var13);
         throw new OperateRuntimeException(message, var13);
      }
   }

   public Map collectFlowNodeDataForPropagatedIncidents(List incidents, String processInstanceId, String currentTreePath) {
      Set flowNodeInstanceIdsSet = new HashSet();
      Map incDatas = new HashMap();

      IncidentEntity inc;
      IncidentDataHolder incData;
      for(Iterator var6 = incidents.iterator(); var6.hasNext(); incDatas.put(inc.getId(), incData)) {
         inc = (IncidentEntity)var6.next();
         incData = (new IncidentDataHolder()).setIncidentId(inc.getId());
         if (!String.valueOf(inc.getProcessInstanceKey()).equals(processInstanceId)) {
            String callActivityInstanceId = TreePath.extractFlowNodeInstanceId(inc.getTreePath(), currentTreePath);
            incData.setFinalFlowNodeInstanceId(callActivityInstanceId);
            flowNodeInstanceIdsSet.add(callActivityInstanceId);
         } else {
            incData.setFinalFlowNodeInstanceId(String.valueOf(inc.getFlowNodeInstanceKey()));
            incData.setFinalFlowNodeId(inc.getFlowNodeId());
         }
      }

      if (flowNodeInstanceIdsSet.size() > 0) {
         Map flowNodeIdsMap = this.getFlowNodeIds(flowNodeInstanceIdsSet);
         incDatas.values().stream().filter((iData) -> {
            return iData.getFinalFlowNodeId() == null;
         }).forEach((iData) -> {
            iData.setFinalFlowNodeId((String)flowNodeIdsMap.get(iData.getFinalFlowNodeInstanceId()));
         });
      }

      return incDatas;
   }

   private Map getFlowNodeIds(Set flowNodeInstanceIds) {
      Map flowNodeIdsMap = new HashMap();
      QueryBuilder q = QueryBuilders.termsQuery("id", flowNodeInstanceIds);
      SearchRequest request = ElasticsearchUtil.createSearchRequest(this.flowNodeInstanceTemplate, QueryType.ONLY_RUNTIME).source((new SearchSourceBuilder()).query(q).fetchSource(new String[]{"id", "flowNodeId"}, (String[])null));

      try {
         ElasticsearchUtil.scrollWith(request, this.esClient, (searchHits) -> {
            Arrays.stream(searchHits.getHits()).forEach((h) -> {
               flowNodeIdsMap.put(h.getId(), (String)h.getSourceAsMap().get("flowNodeId"));
            });
         }, (Consumer)null, (Consumer)null);
         return flowNodeIdsMap;
      } catch (IOException var6) {
         throw new OperateRuntimeException("Exception occurred when searching for flow node ids: " + var6.getMessage(), var6);
      }
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
