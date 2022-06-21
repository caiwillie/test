package io.camunda.operate.webapp.es.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.entities.FlowNodeState;
import io.camunda.operate.entities.FlowNodeType;
import io.camunda.operate.entities.listview.ProcessInstanceForListViewEntity;
import io.camunda.operate.entities.listview.ProcessInstanceState;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.schema.templates.ListViewTemplate;
import io.camunda.operate.util.CollectionUtil;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.util.ElasticsearchUtil.QueryType;
import io.camunda.operate.webapp.rest.dto.listview.ListViewProcessInstanceDto;
import io.camunda.operate.webapp.rest.dto.listview.ListViewQueryDto;
import io.camunda.operate.webapp.rest.dto.listview.ListViewRequestDto;
import io.camunda.operate.webapp.rest.dto.listview.ListViewResponseDto;
import io.camunda.operate.webapp.rest.dto.listview.VariablesQueryDto;
import io.camunda.operate.webapp.rest.exception.InvalidRequestException;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.ConstantScoreQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.join.query.JoinQueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ListViewReader {
   private static final String WILD_CARD = "*";
   private static final Logger logger = LoggerFactory.getLogger(ListViewReader.class);
   @Autowired
   private RestHighLevelClient esClient;
   @Autowired
   private ObjectMapper objectMapper;
   @Autowired
   private ListViewTemplate listViewTemplate;
   @Autowired
   private OperateProperties operateProperties;
   @Autowired
   private DateTimeFormatter dateTimeFormatter;
   @Autowired
   private OperationReader operationReader;

   public ListViewResponseDto queryProcessInstances(ListViewRequestDto processInstanceRequest) {
      ListViewResponseDto result = new ListViewResponseDto();
      List processInstanceEntities = this.queryListView(processInstanceRequest, result);
      List processInstanceKeys = CollectionUtil.map(processInstanceEntities, (processInstanceEntity) -> {
         return Long.valueOf(processInstanceEntity.getId());
      });
      Map operationsPerProcessInstance = this.operationReader.getOperationsPerProcessInstanceKey(processInstanceKeys);
      List processInstanceDtoList = ListViewProcessInstanceDto.createFrom(processInstanceEntities, operationsPerProcessInstance);
      result.setProcessInstances(processInstanceDtoList);
      return result;
   }

   public List queryListView(ListViewRequestDto processInstanceRequest, ListViewResponseDto result) {
      QueryBuilder query = this.createRequestQuery(processInstanceRequest.getQuery());
      logger.debug("Process instance search request: \n{}", query.toString());
      SearchSourceBuilder searchSourceBuilder = (new SearchSourceBuilder()).query(query);
      this.applySorting(searchSourceBuilder, processInstanceRequest);
      SearchRequest searchRequest = this.createSearchRequest(processInstanceRequest.getQuery()).source(searchSourceBuilder);
      logger.debug("Search request will search in: \n{}", searchRequest.indices());

      try {
         SearchResponse response = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
         result.setTotalCount(response.getHits().getTotalHits().value);
         List processInstanceEntities = ElasticsearchUtil.mapSearchHits(response.getHits().getHits(), (sh) -> {
            ProcessInstanceForListViewEntity entity = (ProcessInstanceForListViewEntity)ElasticsearchUtil.fromSearchHit(sh.getSourceAsString(), this.objectMapper, ProcessInstanceForListViewEntity.class);
            entity.setSortValues(sh.getSortValues());
            return entity;
         });
         if (processInstanceRequest.getSearchBefore() != null) {
            Collections.reverse(processInstanceEntities);
         }

         return processInstanceEntities;
      } catch (IOException var8) {
         String message = String.format("Exception occurred, while obtaining instances list: %s", var8.getMessage());
         logger.error(message, var8);
         throw new OperateRuntimeException(message, var8);
      }
   }

   private void applySorting(SearchSourceBuilder searchSourceBuilder, ListViewRequestDto request) {
      String sortBy = this.getSortBy(request);
      boolean directSorting = request.getSearchAfter() != null || request.getSearchBefore() == null;
      if (request.getSorting() != null) {
         SortOrder sort1DirectOrder = SortOrder.fromString(request.getSorting().getSortOrder());
         FieldSortBuilder sort1;
         if (directSorting) {
            sort1 = ((FieldSortBuilder)SortBuilders.fieldSort(sortBy).order(sort1DirectOrder)).missing("_last");
         } else {
            sort1 = ((FieldSortBuilder)SortBuilders.fieldSort(sortBy).order(this.reverseOrder(sort1DirectOrder))).missing("_first");
         }

         searchSourceBuilder.sort(sort1);
      }

      SortBuilder sort2;
      Object[] querySearchAfter;
      if (directSorting) {
         sort2 = SortBuilders.fieldSort("key").order(SortOrder.ASC);
         querySearchAfter = request.getSearchAfter();
      } else {
         sort2 = SortBuilders.fieldSort("key").order(SortOrder.DESC);
         querySearchAfter = request.getSearchBefore();
      }

      searchSourceBuilder.sort(sort2).size(request.getPageSize());
      if (querySearchAfter != null) {
         searchSourceBuilder.searchAfter(querySearchAfter);
      }

   }

   private String getSortBy(ListViewRequestDto request) {
      if (request.getSorting() != null) {
         String sortBy = request.getSorting().getSortBy();
         if (sortBy.equals("parentInstanceId")) {
            sortBy = "parentProcessInstanceKey";
         } else if (sortBy.equals("id")) {
            sortBy = "key";
         }

         return sortBy;
      } else {
         return null;
      }
   }

   private SortOrder reverseOrder(SortOrder sortOrder) {
      return sortOrder.equals(SortOrder.ASC) ? SortOrder.DESC : SortOrder.ASC;
   }

   private SearchRequest createSearchRequest(ListViewQueryDto processInstanceRequest) {
      return processInstanceRequest.isFinished() ? ElasticsearchUtil.createSearchRequest(this.listViewTemplate, QueryType.ALL) : ElasticsearchUtil.createSearchRequest(this.listViewTemplate, QueryType.ONLY_RUNTIME);
   }

   private QueryBuilder createRequestQuery(ListViewQueryDto request) {
      QueryBuilder query = this.createQueryFragment(request);
      TermQueryBuilder isProcessInstanceQuery = QueryBuilders.termQuery("joinRelation", "processInstance");
      QueryBuilder queryBuilder = ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{isProcessInstanceQuery, query});
      return QueryBuilders.constantScoreQuery(queryBuilder);
   }

   public ConstantScoreQueryBuilder createProcessInstancesQuery(ListViewQueryDto query) {
      TermQueryBuilder isProcessInstanceQuery = QueryBuilders.termQuery("joinRelation", "processInstance");
      QueryBuilder queryBuilder = ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{isProcessInstanceQuery, this.createQueryFragment(query)});
      return QueryBuilders.constantScoreQuery(queryBuilder);
   }

   public QueryBuilder createQueryFragment(ListViewQueryDto query) {
      return this.createQueryFragment(query, QueryType.ALL);
   }

   public QueryBuilder createQueryFragment(ListViewQueryDto query, ElasticsearchUtil.QueryType queryType) {
      return ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{this.createRunningFinishedQuery(query, queryType), this.createActivityIdQuery(query, queryType), this.createIdsQuery(query), this.createErrorMessageQuery(query), this.createStartDateQuery(query), this.createEndDateQuery(query), this.createProcessDefinitionKeysQuery(query), this.createBpmnProcessIdQuery(query), this.createExcludeIdsQuery(query), this.createVariablesQuery(query), this.createBatchOperatioIdQuery(query), this.createParentInstanceIdQuery(query)});
   }

   private QueryBuilder createBatchOperatioIdQuery(ListViewQueryDto query) {
      return query.getBatchOperationId() != null ? QueryBuilders.termQuery("batchOperationIds", query.getBatchOperationId()) : null;
   }

   private QueryBuilder createParentInstanceIdQuery(ListViewQueryDto query) {
      return query.getParentInstanceId() != null ? QueryBuilders.termQuery("parentProcessInstanceKey", query.getParentInstanceId()) : null;
   }

   private QueryBuilder createProcessDefinitionKeysQuery(ListViewQueryDto query) {
      return CollectionUtil.isNotEmpty(query.getProcessIds()) ? QueryBuilders.termsQuery("processDefinitionKey", query.getProcessIds()) : null;
   }

   private QueryBuilder createBpmnProcessIdQuery(ListViewQueryDto query) {
      if (!StringUtils.isEmpty(query.getBpmnProcessId())) {
         TermQueryBuilder bpmnProcessIdQ = QueryBuilders.termQuery("bpmnProcessId", query.getBpmnProcessId());
         TermQueryBuilder versionQ = null;
         if (query.getProcessVersion() != null) {
            versionQ = QueryBuilders.termQuery("processVersion", query.getProcessVersion());
         }

         return ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{bpmnProcessIdQ, versionQ});
      } else {
         return null;
      }
   }

   private QueryBuilder createVariablesQuery(ListViewQueryDto query) {
      VariablesQueryDto variablesQuery = query.getVariable();
      if (variablesQuery != null && !StringUtils.isEmpty(variablesQuery.getName())) {
         if (variablesQuery.getName() == null) {
            throw new InvalidRequestException("Variables query must provide not-null variable name.");
         } else {
            return JoinQueryBuilders.hasChildQuery("variable", ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{QueryBuilders.termQuery("varName", variablesQuery.getName()), QueryBuilders.termQuery("varValue", variablesQuery.getValue())}), ScoreMode.None);
         }
      } else {
         return null;
      }
   }

   private QueryBuilder createExcludeIdsQuery(ListViewQueryDto query) {
      return CollectionUtil.isNotEmpty(query.getExcludeIds()) ? QueryBuilders.boolQuery().mustNot(QueryBuilders.termsQuery("id", query.getExcludeIds())) : null;
   }

   private QueryBuilder createEndDateQuery(ListViewQueryDto query) {
      if (query.getEndDateAfter() == null && query.getEndDateBefore() == null) {
         return null;
      } else {
         RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("endDate");
         if (query.getEndDateAfter() != null) {
            rangeQueryBuilder.gte(this.dateTimeFormatter.format(query.getEndDateAfter()));
         }

         if (query.getEndDateBefore() != null) {
            rangeQueryBuilder.lt(this.dateTimeFormatter.format(query.getEndDateBefore()));
         }

         rangeQueryBuilder.format(this.operateProperties.getElasticsearch().getElsDateFormat());
         return rangeQueryBuilder;
      }
   }

   private QueryBuilder createStartDateQuery(ListViewQueryDto query) {
      if (query.getStartDateAfter() == null && query.getStartDateBefore() == null) {
         return null;
      } else {
         RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("startDate");
         if (query.getStartDateAfter() != null) {
            rangeQueryBuilder.gte(this.dateTimeFormatter.format(query.getStartDateAfter()));
         }

         if (query.getStartDateBefore() != null) {
            rangeQueryBuilder.lt(this.dateTimeFormatter.format(query.getStartDateBefore()));
         }

         rangeQueryBuilder.format(this.operateProperties.getElasticsearch().getElsDateFormat());
         return rangeQueryBuilder;
      }
   }

   private QueryBuilder createErrorMessageAsAndMatchQuery(String errorMessage) {
      return JoinQueryBuilders.hasChildQuery("activity", QueryBuilders.matchQuery("errorMessage", errorMessage).operator(Operator.AND), ScoreMode.None);
   }

   private QueryBuilder createErrorMessageAsWildcardQuery(String errorMessage) {
      return JoinQueryBuilders.hasChildQuery("activity", QueryBuilders.wildcardQuery("errorMessage", errorMessage), ScoreMode.None);
   }

   private QueryBuilder createErrorMessageQuery(ListViewQueryDto query) {
      String errorMessage = query.getErrorMessage();
      if (!StringUtils.isEmpty(errorMessage)) {
         return errorMessage.contains("*") ? this.createErrorMessageAsWildcardQuery(errorMessage.toLowerCase()) : this.createErrorMessageAsAndMatchQuery(errorMessage);
      } else {
         return null;
      }
   }

   private QueryBuilder createIdsQuery(ListViewQueryDto query) {
      return CollectionUtil.isNotEmpty(query.getIds()) ? QueryBuilders.termsQuery("id", query.getIds()) : null;
   }

   private QueryBuilder createRunningFinishedQuery(ListViewQueryDto query, ElasticsearchUtil.QueryType queryType) {
      boolean active = query.isActive();
      boolean incidents = query.isIncidents();
      boolean running = query.isRunning();
      boolean completed = query.isCompleted();
      boolean canceled = query.isCanceled();
      boolean finished = query.isFinished();
      if (!running && !finished) {
         return ElasticsearchUtil.createMatchNoneQuery();
      } else if (running && finished && active && incidents && completed && canceled) {
         return null;
      } else {
         QueryBuilder runningQuery = null;
         QueryBuilder completedQuery;
         if (running && (active || incidents)) {
            runningQuery = QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("endDate"));
            QueryBuilder activeQuery = this.createActiveQuery(query);
            completedQuery = this.createIncidentsQuery(query);
            if (query.getActivityId() != null || !query.isActive() || !query.isIncidents()) {
               runningQuery = ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{(QueryBuilder)runningQuery, ElasticsearchUtil.joinWithOr(new QueryBuilder[]{activeQuery, completedQuery})});
            }
         }

         QueryBuilder finishedQuery = null;
         if (finished && (completed || canceled)) {
            finishedQuery = QueryBuilders.existsQuery("endDate");
            completedQuery = this.createCompletedQuery(query);
            QueryBuilder canceledQuery = this.createCanceledQuery(query);
            if (query.getActivityId() != null || !query.isCompleted() || !query.isCanceled()) {
               finishedQuery = ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{(QueryBuilder)finishedQuery, ElasticsearchUtil.joinWithOr(new QueryBuilder[]{completedQuery, canceledQuery})});
            }
         }

         completedQuery = ElasticsearchUtil.joinWithOr(new QueryBuilder[]{(QueryBuilder)runningQuery, (QueryBuilder)finishedQuery});
         return (QueryBuilder)(completedQuery == null ? ElasticsearchUtil.createMatchNoneQuery() : completedQuery);
      }
   }

   private QueryBuilder createActivityIdQuery(ListViewQueryDto query, ElasticsearchUtil.QueryType queryType) {
      if (StringUtils.isEmpty(query.getActivityId())) {
         return null;
      } else {
         QueryBuilder activeActivityIdQuery = null;
         if (query.isActive()) {
            activeActivityIdQuery = this.createActivityIdQuery(query.getActivityId(), FlowNodeState.ACTIVE);
         }

         QueryBuilder incidentActivityIdQuery = null;
         if (query.isIncidents()) {
            incidentActivityIdQuery = this.createActivityIdIncidentQuery(query.getActivityId());
         }

         QueryBuilder completedActivityIdQuery = null;
         if (query.isCompleted()) {
            completedActivityIdQuery = this.createActivityIdQuery(query.getActivityId(), FlowNodeState.COMPLETED);
         }

         QueryBuilder canceledActivityIdQuery = null;
         if (query.isCanceled()) {
            canceledActivityIdQuery = this.createActivityIdQuery(query.getActivityId(), FlowNodeState.TERMINATED);
         }

         return ElasticsearchUtil.joinWithOr(new QueryBuilder[]{activeActivityIdQuery, incidentActivityIdQuery, completedActivityIdQuery, canceledActivityIdQuery});
      }
   }

   private QueryBuilder createCanceledQuery(ListViewQueryDto query) {
      return query.isCanceled() ? QueryBuilders.termQuery("state", ProcessInstanceState.CANCELED.toString()) : null;
   }

   private QueryBuilder createCompletedQuery(ListViewQueryDto query) {
      return query.isCompleted() ? QueryBuilders.termQuery("state", ProcessInstanceState.COMPLETED.toString()) : null;
   }

   private QueryBuilder createIncidentsQuery(ListViewQueryDto query) {
      return query.isIncidents() ? QueryBuilders.termQuery("incident", true) : null;
   }

   private QueryBuilder createActiveQuery(ListViewQueryDto query) {
      return query.isActive() ? QueryBuilders.termQuery("incident", false) : null;
   }

   private QueryBuilder createActivityIdQuery(String activityId, FlowNodeState state) {
      QueryBuilder activitiesQuery = QueryBuilders.termQuery("activityState", state.name());
      QueryBuilder activityIdQuery = QueryBuilders.termQuery("activityId", activityId);
      QueryBuilder activityIsEndNodeQuery = null;
      if (state.equals(FlowNodeState.COMPLETED)) {
         activityIsEndNodeQuery = QueryBuilders.termQuery("activityType", FlowNodeType.END_EVENT.name());
      }

      return JoinQueryBuilders.hasChildQuery("activity", ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{activitiesQuery, activityIdQuery, activityIsEndNodeQuery}), ScoreMode.None);
   }

   private QueryBuilder createActivityIdIncidentQuery(String activityId) {
      QueryBuilder activitiesQuery = QueryBuilders.termQuery("activityState", FlowNodeState.ACTIVE.name());
      QueryBuilder activityIdQuery = QueryBuilders.termQuery("activityId", activityId);
      ExistsQueryBuilder incidentExists = QueryBuilders.existsQuery("errorMessage");
      return JoinQueryBuilders.hasChildQuery("activity", ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{activitiesQuery, activityIdQuery, incidentExists}), ScoreMode.None);
   }
}
