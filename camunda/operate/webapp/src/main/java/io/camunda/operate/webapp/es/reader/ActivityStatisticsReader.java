/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.FlowNodeState
 *  io.camunda.operate.entities.FlowNodeType
 *  io.camunda.operate.exceptions.OperateRuntimeException
 *  io.camunda.operate.schema.templates.ListViewTemplate
 *  io.camunda.operate.schema.templates.TemplateDescriptor
 *  io.camunda.operate.util.CollectionUtil
 *  io.camunda.operate.util.ElasticsearchUtil
 *  io.camunda.operate.util.ElasticsearchUtil$QueryType
 *  io.camunda.operate.webapp.es.reader.ActivityStatisticsReader$MapUpdater
 *  io.camunda.operate.webapp.es.reader.ListViewReader
 *  io.camunda.operate.webapp.rest.dto.FlowNodeStatisticsDto
 *  io.camunda.operate.webapp.rest.dto.listview.ListViewQueryDto
 *  org.elasticsearch.action.search.SearchRequest
 *  org.elasticsearch.action.search.SearchResponse
 *  org.elasticsearch.client.RequestOptions
 *  org.elasticsearch.client.RestHighLevelClient
 *  org.elasticsearch.index.query.ConstantScoreQueryBuilder
 *  org.elasticsearch.index.query.QueryBuilder
 *  org.elasticsearch.index.query.QueryBuilders
 *  org.elasticsearch.join.aggregations.Children
 *  org.elasticsearch.join.aggregations.ChildrenAggregationBuilder
 *  org.elasticsearch.join.aggregations.JoinAggregationBuilders
 *  org.elasticsearch.join.aggregations.Parent
 *  org.elasticsearch.search.aggregations.AggregationBuilder
 *  org.elasticsearch.search.aggregations.AggregationBuilders
 *  org.elasticsearch.search.aggregations.bucket.filter.Filter
 *  org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder
 *  org.elasticsearch.search.aggregations.bucket.terms.Terms
 *  org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder
 *  org.elasticsearch.search.builder.SearchSourceBuilder
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.es.reader;

import io.camunda.operate.entities.FlowNodeState;
import io.camunda.operate.entities.FlowNodeType;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.schema.templates.ListViewTemplate;
import io.camunda.operate.schema.templates.TemplateDescriptor;
import io.camunda.operate.util.CollectionUtil;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.webapp.es.reader.ActivityStatisticsReader;
import io.camunda.operate.webapp.es.reader.ListViewReader;
import io.camunda.operate.webapp.rest.dto.FlowNodeStatisticsDto;
import io.camunda.operate.webapp.rest.dto.listview.ListViewQueryDto;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.ConstantScoreQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.join.aggregations.Children;
import org.elasticsearch.join.aggregations.ChildrenAggregationBuilder;
import org.elasticsearch.join.aggregations.JoinAggregationBuilders;
import org.elasticsearch.join.aggregations.Parent;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ActivityStatisticsReader {
    private static final Logger logger = LoggerFactory.getLogger(ActivityStatisticsReader.class);
    public static final String AGG_ACTIVITIES = "activities";
    public static final String AGG_UNIQUE_ACTIVITIES = "unique_activities";
    public static final String AGG_ACTIVITY_TO_PROCESS = "activity_to_process";
    public static final String AGG_ACTIVE_ACTIVITIES = "active_activities";
    public static final String AGG_INCIDENT_ACTIVITIES = "incident_activities";
    public static final String AGG_TERMINATED_ACTIVITIES = "terminated_activities";
    public static final String AGG_FINISHED_ACTIVITIES = "finished_activities";
    @Autowired
    private RestHighLevelClient esClient;
    @Autowired
    private ListViewReader listViewReader;
    @Autowired
    private ListViewTemplate listViewTemplate;

    public Collection<FlowNodeStatisticsDto> getFlowNodeStatistics(ListViewQueryDto query) {
        SearchRequest searchRequest = !query.isFinished() ? this.createQuery(query, ElasticsearchUtil.QueryType.ONLY_RUNTIME) : this.createQuery(query, ElasticsearchUtil.QueryType.ALL);
        Map<String, FlowNodeStatisticsDto> statisticsMap = this.runQueryAndCollectStats(searchRequest);
        return statisticsMap.values();
    }

    public Map<String, FlowNodeStatisticsDto> runQueryAndCollectStats(SearchRequest searchRequest) {
        try {
            HashMap<String, FlowNodeStatisticsDto> statisticsMap = new HashMap<String, FlowNodeStatisticsDto>();
            SearchResponse searchResponse = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
            if (searchResponse.getAggregations() == null) return statisticsMap;
            Children activities = (Children)searchResponse.getAggregations().get(AGG_ACTIVITIES);

            CollectionUtil.asMap((Object[])new Object[]{
                    AGG_ACTIVE_ACTIVITIES, (MapUpdater)FlowNodeStatisticsDto::addActive,
                    AGG_INCIDENT_ACTIVITIES, (MapUpdater)FlowNodeStatisticsDto::addIncidents,
                    AGG_TERMINATED_ACTIVITIES, (MapUpdater)FlowNodeStatisticsDto::addCanceled,
                    AGG_FINISHED_ACTIVITIES, (MapUpdater)FlowNodeStatisticsDto::addCompleted})
                    .forEach((aggName, mapUpdater) -> this.collectStatisticsFor((Map<String, FlowNodeStatisticsDto>)statisticsMap, activities, (String)aggName, (MapUpdater)mapUpdater));
            return statisticsMap;
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining statistics for activities: %s", e.getMessage());
            logger.error(message, e);
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    public SearchRequest createQuery(ListViewQueryDto query, ElasticsearchUtil.QueryType queryType) {
        ConstantScoreQueryBuilder q = QueryBuilders.constantScoreQuery((QueryBuilder)this.listViewReader.createQueryFragment(query, queryType));
        ChildrenAggregationBuilder agg = JoinAggregationBuilders.children((String)AGG_ACTIVITIES, (String)"activity");
        if (query.isActive()) {
            agg = (ChildrenAggregationBuilder)agg.subAggregation((AggregationBuilder)this.getActiveFlowNodesAgg());
        }
        if (query.isCanceled()) {
            agg = (ChildrenAggregationBuilder)agg.subAggregation((AggregationBuilder)this.getTerminatedActivitiesAgg());
        }
        if (query.isIncidents()) {
            agg = (ChildrenAggregationBuilder)agg.subAggregation((AggregationBuilder)this.getIncidentActivitiesAgg());
        }
        agg = (ChildrenAggregationBuilder)agg.subAggregation((AggregationBuilder)this.getFinishedActivitiesAgg());
        logger.debug("Activities statistics request: \n{}\n and aggregation: \n{}", (Object)q.toString(), (Object)agg.toString());
        SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.listViewTemplate, (ElasticsearchUtil.QueryType)queryType);
        logger.debug("Search request will search in: \n{}", searchRequest.indices());
        return searchRequest.source(new SearchSourceBuilder().query((QueryBuilder)q).size(0).aggregation((AggregationBuilder)agg));
    }

    private void collectStatisticsFor(Map<String, FlowNodeStatisticsDto> statisticsMap, Children activities, String aggName, MapUpdater mapUpdater) {
        Filter incidentActivitiesAgg = (Filter)activities.getAggregations().get(aggName);
        if (incidentActivitiesAgg == null) return;
        ((Terms)incidentActivitiesAgg.getAggregations().get(AGG_UNIQUE_ACTIVITIES)).getBuckets().stream().forEach(b -> {
            String activityId = b.getKeyAsString();
            Parent aggregation = (Parent)b.getAggregations().get(AGG_ACTIVITY_TO_PROCESS);
            long docCount = aggregation.getDocCount();
            if (statisticsMap.get(activityId) == null) {
                statisticsMap.put(activityId, new FlowNodeStatisticsDto(activityId));
            }
            mapUpdater.updateMapEntry((FlowNodeStatisticsDto)statisticsMap.get(activityId), Long.valueOf(docCount));
        });
    }

    private FilterAggregationBuilder getTerminatedActivitiesAgg() {
        return (FilterAggregationBuilder)AggregationBuilders.filter((String)AGG_TERMINATED_ACTIVITIES, (QueryBuilder)QueryBuilders.termQuery((String)"activityState", (Object)FlowNodeState.TERMINATED)).subAggregation((AggregationBuilder)((TermsAggregationBuilder)AggregationBuilders.terms((String)AGG_UNIQUE_ACTIVITIES).field("activityId")).size(10000).subAggregation((AggregationBuilder)JoinAggregationBuilders.parent((String)AGG_ACTIVITY_TO_PROCESS, (String)"activity")));
    }

    private FilterAggregationBuilder getActiveFlowNodesAgg() {
        return (FilterAggregationBuilder)AggregationBuilders.filter((String)AGG_ACTIVE_ACTIVITIES, (QueryBuilder)QueryBuilders.boolQuery().must((QueryBuilder)QueryBuilders.termQuery((String)"incident", (boolean)false)).must((QueryBuilder)QueryBuilders.termQuery((String)"activityState", (String)FlowNodeState.ACTIVE.toString()))).subAggregation((AggregationBuilder)((TermsAggregationBuilder)AggregationBuilders.terms((String)AGG_UNIQUE_ACTIVITIES).field("activityId")).size(10000).subAggregation((AggregationBuilder)JoinAggregationBuilders.parent((String)AGG_ACTIVITY_TO_PROCESS, (String)"activity")));
    }

    private FilterAggregationBuilder getIncidentActivitiesAgg() {
        return (FilterAggregationBuilder)AggregationBuilders.filter((String)AGG_INCIDENT_ACTIVITIES, (QueryBuilder)QueryBuilders.boolQuery().must((QueryBuilder)QueryBuilders.termQuery((String)"incident", (boolean)true)).must((QueryBuilder)QueryBuilders.termQuery((String)"activityState", (String)FlowNodeState.ACTIVE.toString()))).subAggregation((AggregationBuilder)((TermsAggregationBuilder)AggregationBuilders.terms((String)AGG_UNIQUE_ACTIVITIES).field("activityId")).size(10000).subAggregation((AggregationBuilder)JoinAggregationBuilders.parent((String)AGG_ACTIVITY_TO_PROCESS, (String)"activity")));
    }

    private FilterAggregationBuilder getFinishedActivitiesAgg() {
        QueryBuilder completedEndEventsQ = ElasticsearchUtil.joinWithAnd((QueryBuilder[])new QueryBuilder[]{QueryBuilders.termQuery((String)"activityType", (String)FlowNodeType.END_EVENT.toString()), QueryBuilders.termQuery((String)"activityState", (String)FlowNodeState.COMPLETED.toString())});
        return (FilterAggregationBuilder)AggregationBuilders.filter((String)AGG_FINISHED_ACTIVITIES, (QueryBuilder)completedEndEventsQ).subAggregation((AggregationBuilder)((TermsAggregationBuilder)AggregationBuilders.terms((String)AGG_UNIQUE_ACTIVITIES).field("activityId")).size(10000).subAggregation((AggregationBuilder)JoinAggregationBuilders.parent((String)AGG_ACTIVITY_TO_PROCESS, (String)"activity")));
    }

    @FunctionalInterface
    private static interface MapUpdater {
        public void updateMapEntry(FlowNodeStatisticsDto var1, Long var2);
    }
}
