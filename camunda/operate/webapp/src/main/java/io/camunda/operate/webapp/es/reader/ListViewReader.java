/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  io.camunda.operate.entities.FlowNodeState
 *  io.camunda.operate.entities.FlowNodeType
 *  io.camunda.operate.entities.listview.ProcessInstanceForListViewEntity
 *  io.camunda.operate.entities.listview.ProcessInstanceState
 *  io.camunda.operate.exceptions.OperateRuntimeException
 *  io.camunda.operate.property.OperateProperties
 *  io.camunda.operate.schema.templates.ListViewTemplate
 *  io.camunda.operate.schema.templates.TemplateDescriptor
 *  io.camunda.operate.util.CollectionUtil
 *  io.camunda.operate.util.ElasticsearchUtil
 *  io.camunda.operate.util.ElasticsearchUtil$QueryType
 *  io.camunda.operate.webapp.es.reader.OperationReader
 *  io.camunda.operate.webapp.rest.dto.listview.ListViewProcessInstanceDto
 *  io.camunda.operate.webapp.rest.dto.listview.ListViewQueryDto
 *  io.camunda.operate.webapp.rest.dto.listview.ListViewRequestDto
 *  io.camunda.operate.webapp.rest.dto.listview.ListViewResponseDto
 *  io.camunda.operate.webapp.rest.dto.listview.VariablesQueryDto
 *  io.camunda.operate.webapp.rest.exception.InvalidRequestException
 *  org.apache.lucene.search.join.ScoreMode
 *  org.elasticsearch.action.search.SearchRequest
 *  org.elasticsearch.action.search.SearchResponse
 *  org.elasticsearch.client.RequestOptions
 *  org.elasticsearch.client.RestHighLevelClient
 *  org.elasticsearch.index.query.BoolQueryBuilder
 *  org.elasticsearch.index.query.ConstantScoreQueryBuilder
 *  org.elasticsearch.index.query.ExistsQueryBuilder
 *  org.elasticsearch.index.query.Operator
 *  org.elasticsearch.index.query.QueryBuilder
 *  org.elasticsearch.index.query.QueryBuilders
 *  org.elasticsearch.index.query.RangeQueryBuilder
 *  org.elasticsearch.index.query.TermQueryBuilder
 *  org.elasticsearch.join.query.JoinQueryBuilders
 *  org.elasticsearch.search.SearchHit
 *  org.elasticsearch.search.builder.SearchSourceBuilder
 *  org.elasticsearch.search.sort.FieldSortBuilder
 *  org.elasticsearch.search.sort.SortBuilder
 *  org.elasticsearch.search.sort.SortBuilders
 *  org.elasticsearch.search.sort.SortOrder
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 *  org.springframework.util.StringUtils
 */
package io.camunda.operate.webapp.es.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.entities.FlowNodeState;
import io.camunda.operate.entities.FlowNodeType;
import io.camunda.operate.entities.listview.ProcessInstanceForListViewEntity;
import io.camunda.operate.entities.listview.ProcessInstanceState;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.schema.templates.ListViewTemplate;
import io.camunda.operate.schema.templates.TemplateDescriptor;
import io.camunda.operate.util.CollectionUtil;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.webapp.es.reader.OperationReader;
import io.camunda.operate.webapp.rest.dto.listview.ListViewProcessInstanceDto;
import io.camunda.operate.webapp.rest.dto.listview.ListViewQueryDto;
import io.camunda.operate.webapp.rest.dto.listview.ListViewRequestDto;
import io.camunda.operate.webapp.rest.dto.listview.ListViewResponseDto;
import io.camunda.operate.webapp.rest.dto.listview.VariablesQueryDto;
import io.camunda.operate.webapp.rest.exception.InvalidRequestException;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ConstantScoreQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.join.query.JoinQueryBuilders;
import org.elasticsearch.search.SearchHit;
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
        List<ProcessInstanceForListViewEntity> processInstanceEntities = this.queryListView(processInstanceRequest, result);
        List processInstanceKeys = CollectionUtil.map(processInstanceEntities, processInstanceEntity -> Long.valueOf(processInstanceEntity.getId()));
        Map operationsPerProcessInstance = this.operationReader.getOperationsPerProcessInstanceKey(processInstanceKeys);
        List processInstanceDtoList = ListViewProcessInstanceDto.createFrom(processInstanceEntities, (Map)operationsPerProcessInstance);
        result.setProcessInstances(processInstanceDtoList);
        return result;
    }

    public List<ProcessInstanceForListViewEntity> queryListView(ListViewRequestDto processInstanceRequest, ListViewResponseDto result) {
        QueryBuilder query = this.createRequestQuery(processInstanceRequest.getQuery());
        logger.debug("Process instance search request: \n{}", (Object)query.toString());
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(query);
        this.applySorting(searchSourceBuilder, processInstanceRequest);
        SearchRequest searchRequest = this.createSearchRequest(processInstanceRequest.getQuery()).source(searchSourceBuilder);
        logger.debug("Search request will search in: \n{}", searchRequest.indices());
        try {
            SearchResponse response = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
            result.setTotalCount(response.getHits().getTotalHits().value);
            List processInstanceEntities = ElasticsearchUtil.mapSearchHits((SearchHit[])response.getHits().getHits(), sh -> {
                ProcessInstanceForListViewEntity entity = (ProcessInstanceForListViewEntity)ElasticsearchUtil.fromSearchHit((String)sh.getSourceAsString(), (ObjectMapper)this.objectMapper, ProcessInstanceForListViewEntity.class);
                entity.setSortValues(sh.getSortValues());
                return entity;
            });
            if (processInstanceRequest.getSearchBefore() == null) return processInstanceEntities;
            Collections.reverse(processInstanceEntities);
            return processInstanceEntities;
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining instances list: %s", e.getMessage());
            logger.error(message, e);
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    private void applySorting(SearchSourceBuilder searchSourceBuilder, ListViewRequestDto request) {
        Object[] querySearchAfter;
        SortBuilder sort2;
        boolean directSorting;
        String sortBy = this.getSortBy(request);
        boolean bl = directSorting = request.getSearchAfter() != null || request.getSearchBefore() == null;
        if (request.getSorting() != null) {
            SortOrder sort1DirectOrder = SortOrder.fromString((String)request.getSorting().getSortOrder());
            FieldSortBuilder sort1 = directSorting ? ((FieldSortBuilder)SortBuilders.fieldSort((String)sortBy).order(sort1DirectOrder)).missing((Object)"_last") : ((FieldSortBuilder)SortBuilders.fieldSort((String)sortBy).order(this.reverseOrder(sort1DirectOrder))).missing((Object)"_first");
            searchSourceBuilder.sort((SortBuilder)sort1);
        }
        if (directSorting) {
            sort2 = SortBuilders.fieldSort((String)"key").order(SortOrder.ASC);
            querySearchAfter = request.getSearchAfter();
        } else {
            sort2 = SortBuilders.fieldSort((String)"key").order(SortOrder.DESC);
            querySearchAfter = request.getSearchBefore();
        }
        searchSourceBuilder.sort(sort2).size(request.getPageSize().intValue());
        if (querySearchAfter == null) return;
        searchSourceBuilder.searchAfter(querySearchAfter);
    }

    private String getSortBy(ListViewRequestDto request) {
        if (request.getSorting() == null) return null;
        String sortBy = request.getSorting().getSortBy();
        if (sortBy.equals("parentInstanceId")) {
            sortBy = "parentProcessInstanceKey";
        } else {
            if (!sortBy.equals("id")) return sortBy;
            sortBy = "key";
        }
        return sortBy;
    }

    private SortOrder reverseOrder(SortOrder sortOrder) {
        if (!sortOrder.equals((Object)SortOrder.ASC)) return SortOrder.ASC;
        return SortOrder.DESC;
    }

    private SearchRequest createSearchRequest(ListViewQueryDto processInstanceRequest) {
        if (!processInstanceRequest.isFinished()) return ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.listViewTemplate, (ElasticsearchUtil.QueryType)ElasticsearchUtil.QueryType.ONLY_RUNTIME);
        return ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.listViewTemplate, (ElasticsearchUtil.QueryType)ElasticsearchUtil.QueryType.ALL);
    }

    private QueryBuilder createRequestQuery(ListViewQueryDto request) {
        QueryBuilder query = this.createQueryFragment(request);
        TermQueryBuilder isProcessInstanceQuery = QueryBuilders.termQuery((String)"joinRelation", (String)"processInstance");
        QueryBuilder queryBuilder = ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{isProcessInstanceQuery, query});
        return QueryBuilders.constantScoreQuery((QueryBuilder)queryBuilder);
    }

    public ConstantScoreQueryBuilder createProcessInstancesQuery(ListViewQueryDto query) {
        TermQueryBuilder isProcessInstanceQuery = QueryBuilders.termQuery((String)"joinRelation", (String)"processInstance");
        QueryBuilder queryBuilder = ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{isProcessInstanceQuery, this.createQueryFragment(query)});
        return QueryBuilders.constantScoreQuery((QueryBuilder)queryBuilder);
    }

    public QueryBuilder createQueryFragment(ListViewQueryDto query) {
        return this.createQueryFragment(query, ElasticsearchUtil.QueryType.ALL);
    }

    public QueryBuilder createQueryFragment(ListViewQueryDto query, ElasticsearchUtil.QueryType queryType) {
        return ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{this.createRunningFinishedQuery(query, queryType), this.createActivityIdQuery(query, queryType), this.createIdsQuery(query), this.createErrorMessageQuery(query), this.createStartDateQuery(query), this.createEndDateQuery(query), this.createProcessDefinitionKeysQuery(query), this.createBpmnProcessIdQuery(query), this.createExcludeIdsQuery(query), this.createVariablesQuery(query), this.createBatchOperatioIdQuery(query), this.createParentInstanceIdQuery(query)});
    }

    private QueryBuilder createBatchOperatioIdQuery(ListViewQueryDto query) {
        if (query.getBatchOperationId() == null) return null;
        return QueryBuilders.termQuery((String)"batchOperationIds", (String)query.getBatchOperationId());
    }

    private QueryBuilder createParentInstanceIdQuery(ListViewQueryDto query) {
        if (query.getParentInstanceId() == null) return null;
        return QueryBuilders.termQuery((String)"parentProcessInstanceKey", (Object)query.getParentInstanceId());
    }

    private QueryBuilder createProcessDefinitionKeysQuery(ListViewQueryDto query) {
        if (!CollectionUtil.isNotEmpty((Collection)query.getProcessIds())) return null;
        return QueryBuilders.termsQuery((String)"processDefinitionKey", (Collection)query.getProcessIds());
    }

    private QueryBuilder createBpmnProcessIdQuery(ListViewQueryDto query) {
        if (StringUtils.isEmpty((Object)query.getBpmnProcessId())) return null;
        TermQueryBuilder bpmnProcessIdQ = QueryBuilders.termQuery((String)"bpmnProcessId", (String)query.getBpmnProcessId());
        TermQueryBuilder versionQ = null;
        if (query.getProcessVersion() == null) return ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{bpmnProcessIdQ, versionQ});
        versionQ = QueryBuilders.termQuery((String)"processVersion", (Object)query.getProcessVersion());
        return ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{bpmnProcessIdQ, versionQ});
    }

    private QueryBuilder createVariablesQuery(ListViewQueryDto query) {
        VariablesQueryDto variablesQuery = query.getVariable();
        if (variablesQuery == null) return null;
        if (StringUtils.isEmpty((Object)variablesQuery.getName())) return null;
        if (variablesQuery.getName() != null) return JoinQueryBuilders.hasChildQuery((String)"variable", (QueryBuilder)ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{QueryBuilders.termQuery((String)"varName", (String)variablesQuery.getName()), QueryBuilders.termQuery((String)"varValue", (String)variablesQuery.getValue())}), (ScoreMode)ScoreMode.None);
        throw new InvalidRequestException("Variables query must provide not-null variable name.");
    }

    private QueryBuilder createExcludeIdsQuery(ListViewQueryDto query) {
        if (!CollectionUtil.isNotEmpty((Collection)query.getExcludeIds())) return null;
        return QueryBuilders.boolQuery().mustNot((QueryBuilder)QueryBuilders.termsQuery((String)"id", (Collection)query.getExcludeIds()));
    }

    private QueryBuilder createEndDateQuery(ListViewQueryDto query) {
        if (query.getEndDateAfter() == null) {
            if (query.getEndDateBefore() == null) return null;
        }
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery((String)"endDate");
        if (query.getEndDateAfter() != null) {
            rangeQueryBuilder.gte((Object)this.dateTimeFormatter.format(query.getEndDateAfter()));
        }
        if (query.getEndDateBefore() != null) {
            rangeQueryBuilder.lt((Object)this.dateTimeFormatter.format(query.getEndDateBefore()));
        }
        rangeQueryBuilder.format(this.operateProperties.getElasticsearch().getElsDateFormat());
        return rangeQueryBuilder;
    }

    private QueryBuilder createStartDateQuery(ListViewQueryDto query) {
        if (query.getStartDateAfter() == null) {
            if (query.getStartDateBefore() == null) return null;
        }
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery((String)"startDate");
        if (query.getStartDateAfter() != null) {
            rangeQueryBuilder.gte((Object)this.dateTimeFormatter.format(query.getStartDateAfter()));
        }
        if (query.getStartDateBefore() != null) {
            rangeQueryBuilder.lt((Object)this.dateTimeFormatter.format(query.getStartDateBefore()));
        }
        rangeQueryBuilder.format(this.operateProperties.getElasticsearch().getElsDateFormat());
        return rangeQueryBuilder;
    }

    private QueryBuilder createErrorMessageAsAndMatchQuery(String errorMessage) {
        return JoinQueryBuilders.hasChildQuery((String)"activity", (QueryBuilder)QueryBuilders.matchQuery((String)"errorMessage", (Object)errorMessage).operator(Operator.AND), (ScoreMode)ScoreMode.None);
    }

    private QueryBuilder createErrorMessageAsWildcardQuery(String errorMessage) {
        return JoinQueryBuilders.hasChildQuery((String)"activity", (QueryBuilder)QueryBuilders.wildcardQuery((String)"errorMessage", (String)errorMessage), (ScoreMode)ScoreMode.None);
    }

    private QueryBuilder createErrorMessageQuery(ListViewQueryDto query) {
        String errorMessage = query.getErrorMessage();
        if (StringUtils.isEmpty((Object)errorMessage)) return null;
        if (!errorMessage.contains(WILD_CARD)) return this.createErrorMessageAsAndMatchQuery(errorMessage);
        return this.createErrorMessageAsWildcardQuery(errorMessage.toLowerCase());
    }

    private QueryBuilder createIdsQuery(ListViewQueryDto query) {
        if (!CollectionUtil.isNotEmpty((Collection)query.getIds())) return null;
        return QueryBuilders.termsQuery((String)"id", (Collection)query.getIds());
    }

    private QueryBuilder createRunningFinishedQuery(ListViewQueryDto query, ElasticsearchUtil.QueryType queryType) {
        QueryBuilder processInstanceQuery;
        boolean active = query.isActive();
        boolean incidents = query.isIncidents();
        boolean running = query.isRunning();
        boolean completed = query.isCompleted();
        boolean canceled = query.isCanceled();
        boolean finished = query.isFinished();
        if (!running && !finished) {
            return ElasticsearchUtil.createMatchNoneQuery();
        }
        if (running && finished && active && incidents && completed && canceled) {
            return null;
        }
        QueryBuilder runningQuery = null;
        if (running && (active || incidents)) {
            runningQuery = QueryBuilders.boolQuery().mustNot((QueryBuilder)QueryBuilders.existsQuery((String)"endDate"));
            QueryBuilder activeQuery = this.createActiveQuery(query);
            QueryBuilder incidentsQuery = this.createIncidentsQuery(query);
            if (query.getActivityId() != null || !query.isActive() || !query.isIncidents()) {
                runningQuery = ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{runningQuery, ElasticsearchUtil.joinWithOr((QueryBuilder[])new QueryBuilder[]{activeQuery, incidentsQuery})});
            }
        }
        QueryBuilder finishedQuery = null;
        if (finished && (completed || canceled)) {
            finishedQuery = QueryBuilders.existsQuery((String)"endDate");
            QueryBuilder completedQuery = this.createCompletedQuery(query);
            QueryBuilder canceledQuery = this.createCanceledQuery(query);
            if (query.getActivityId() != null || !query.isCompleted() || !query.isCanceled()) {
                finishedQuery = ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{finishedQuery, ElasticsearchUtil.joinWithOr((QueryBuilder[])new QueryBuilder[]{completedQuery, canceledQuery})});
            }
        }
        if ((processInstanceQuery = ElasticsearchUtil.joinWithOr((QueryBuilder[])new QueryBuilder[]{runningQuery, finishedQuery})) != null) return processInstanceQuery;
        return ElasticsearchUtil.createMatchNoneQuery();
    }

    private QueryBuilder createActivityIdQuery(ListViewQueryDto query, ElasticsearchUtil.QueryType queryType) {
        if (StringUtils.isEmpty((Object)query.getActivityId())) {
            return null;
        }
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
        if (!query.isCanceled()) return ElasticsearchUtil.joinWithOr((QueryBuilder[])new QueryBuilder[]{activeActivityIdQuery, incidentActivityIdQuery, completedActivityIdQuery, canceledActivityIdQuery});
        canceledActivityIdQuery = this.createActivityIdQuery(query.getActivityId(), FlowNodeState.TERMINATED);
        return ElasticsearchUtil.joinWithOr((QueryBuilder[])new QueryBuilder[]{activeActivityIdQuery, incidentActivityIdQuery, completedActivityIdQuery, canceledActivityIdQuery});
    }

    private QueryBuilder createCanceledQuery(ListViewQueryDto query) {
        if (!query.isCanceled()) return null;
        return QueryBuilders.termQuery((String)"state", (String)ProcessInstanceState.CANCELED.toString());
    }

    private QueryBuilder createCompletedQuery(ListViewQueryDto query) {
        if (!query.isCompleted()) return null;
        return QueryBuilders.termQuery((String)"state", (String)ProcessInstanceState.COMPLETED.toString());
    }

    private QueryBuilder createIncidentsQuery(ListViewQueryDto query) {
        if (!query.isIncidents()) return null;
        return QueryBuilders.termQuery((String)"incident", (boolean)true);
    }

    private QueryBuilder createActiveQuery(ListViewQueryDto query) {
        if (!query.isActive()) return null;
        return QueryBuilders.termQuery((String)"incident", (boolean)false);
    }

    private QueryBuilder createActivityIdQuery(String activityId, FlowNodeState state) {
        TermQueryBuilder activitiesQuery = QueryBuilders.termQuery((String)"activityState", (String)state.name());
        TermQueryBuilder activityIdQuery = QueryBuilders.termQuery((String)"activityId", (String)activityId);
        TermQueryBuilder activityIsEndNodeQuery = null;
        if (!state.equals((Object)FlowNodeState.COMPLETED)) return JoinQueryBuilders.hasChildQuery((String)"activity", (QueryBuilder)ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{activitiesQuery, activityIdQuery, activityIsEndNodeQuery}), (ScoreMode)ScoreMode.None);
        activityIsEndNodeQuery = QueryBuilders.termQuery((String)"activityType", (String)FlowNodeType.END_EVENT.name());
        return JoinQueryBuilders.hasChildQuery((String)"activity", (QueryBuilder)ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{activitiesQuery, activityIdQuery, activityIsEndNodeQuery}), (ScoreMode)ScoreMode.None);
    }

    private QueryBuilder createActivityIdIncidentQuery(String activityId) {
        TermQueryBuilder activitiesQuery = QueryBuilders.termQuery((String)"activityState", (String)FlowNodeState.ACTIVE.name());
        TermQueryBuilder activityIdQuery = QueryBuilders.termQuery((String)"activityId", (String)activityId);
        ExistsQueryBuilder incidentExists = QueryBuilders.existsQuery((String)"errorMessage");
        return JoinQueryBuilders.hasChildQuery((String)"activity", (QueryBuilder)ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{activitiesQuery, activityIdQuery, incidentExists}), (ScoreMode)ScoreMode.None);
    }
}
