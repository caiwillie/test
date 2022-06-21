package io.camunda.operate.webapp.es.reader;

import io.camunda.operate.entities.FlowNodeState;
import io.camunda.operate.entities.FlowNodeType;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.schema.templates.ListViewTemplate;
import io.camunda.operate.util.CollectionUtil;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.util.ElasticsearchUtil.QueryType;
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
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.join.aggregations.Children;
import org.elasticsearch.join.aggregations.ChildrenAggregationBuilder;
import org.elasticsearch.join.aggregations.JoinAggregationBuilders;
import org.elasticsearch.join.aggregations.Parent;
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

   public Collection getFlowNodeStatistics(ListViewQueryDto query) {
      SearchRequest searchRequest;
      if (!query.isFinished()) {
         searchRequest = this.createQuery(query, QueryType.ONLY_RUNTIME);
      } else {
         searchRequest = this.createQuery(query, QueryType.ALL);
      }

      Map statisticsMap = this.runQueryAndCollectStats(searchRequest);
      return statisticsMap.values();
   }

   public Map runQueryAndCollectStats(SearchRequest searchRequest) {
      try {
         Map statisticsMap = new HashMap();
         SearchResponse searchResponse = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
         if (searchResponse.getAggregations() != null) {
            Children activities = (Children)searchResponse.getAggregations().get("activities");
            CollectionUtil.asMap(new Object[]{"active_activities", FlowNodeStatisticsDto::addActive, "incident_activities", FlowNodeStatisticsDto::addIncidents, "terminated_activities", FlowNodeStatisticsDto::addCanceled, "finished_activities", FlowNodeStatisticsDto::addCompleted}).forEach((aggName, mapUpdater) -> {
               this.collectStatisticsFor(statisticsMap, activities, aggName, (MapUpdater)mapUpdater);
            });
         }

         return statisticsMap;
      } catch (IOException var5) {
         String message = String.format("Exception occurred, while obtaining statistics for activities: %s", var5.getMessage());
         logger.error(message, var5);
         throw new OperateRuntimeException(message, var5);
      }
   }

   public SearchRequest createQuery(ListViewQueryDto query, ElasticsearchUtil.QueryType queryType) {
      QueryBuilder q = QueryBuilders.constantScoreQuery(this.listViewReader.createQueryFragment(query, queryType));
      ChildrenAggregationBuilder agg = JoinAggregationBuilders.children("activities", "activity");
      if (query.isActive()) {
         agg = (ChildrenAggregationBuilder)agg.subAggregation(this.getActiveFlowNodesAgg());
      }

      if (query.isCanceled()) {
         agg = (ChildrenAggregationBuilder)agg.subAggregation(this.getTerminatedActivitiesAgg());
      }

      if (query.isIncidents()) {
         agg = (ChildrenAggregationBuilder)agg.subAggregation(this.getIncidentActivitiesAgg());
      }

      agg = (ChildrenAggregationBuilder)agg.subAggregation(this.getFinishedActivitiesAgg());
      logger.debug("Activities statistics request: \n{}\n and aggregation: \n{}", q.toString(), agg.toString());
      SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest(this.listViewTemplate, queryType);
      logger.debug("Search request will search in: \n{}", searchRequest.indices());
      return searchRequest.source((new SearchSourceBuilder()).query(q).size(0).aggregation(agg));
   }

   private void collectStatisticsFor(Map statisticsMap, Children activities, String aggName, MapUpdater mapUpdater) {
      Filter incidentActivitiesAgg = (Filter)activities.getAggregations().get(aggName);
      if (incidentActivitiesAgg != null) {
         ((Terms)incidentActivitiesAgg.getAggregations().get("unique_activities")).getBuckets().stream().forEach((b) -> {
            String activityId = b.getKeyAsString();
            Parent aggregation = (Parent)b.getAggregations().get("activity_to_process");
            long docCount = aggregation.getDocCount();
            if (statisticsMap.get(activityId) == null) {
               statisticsMap.put(activityId, new FlowNodeStatisticsDto(activityId));
            }

            mapUpdater.updateMapEntry((FlowNodeStatisticsDto)statisticsMap.get(activityId), docCount);
         });
      }

   }

   private FilterAggregationBuilder getTerminatedActivitiesAgg() {
      return (FilterAggregationBuilder)AggregationBuilders.filter("terminated_activities", QueryBuilders.termQuery("activityState", FlowNodeState.TERMINATED)).subAggregation(((TermsAggregationBuilder)AggregationBuilders.terms("unique_activities").field("activityId")).size(10000).subAggregation(JoinAggregationBuilders.parent("activity_to_process", "activity")));
   }

   private FilterAggregationBuilder getActiveFlowNodesAgg() {
      return (FilterAggregationBuilder)AggregationBuilders.filter("active_activities", QueryBuilders.boolQuery().must(QueryBuilders.termQuery("incident", false)).must(QueryBuilders.termQuery("activityState", FlowNodeState.ACTIVE.toString()))).subAggregation(((TermsAggregationBuilder)AggregationBuilders.terms("unique_activities").field("activityId")).size(10000).subAggregation(JoinAggregationBuilders.parent("activity_to_process", "activity")));
   }

   private FilterAggregationBuilder getIncidentActivitiesAgg() {
      return (FilterAggregationBuilder)AggregationBuilders.filter("incident_activities", QueryBuilders.boolQuery().must(QueryBuilders.termQuery("incident", true)).must(QueryBuilders.termQuery("activityState", FlowNodeState.ACTIVE.toString()))).subAggregation(((TermsAggregationBuilder)AggregationBuilders.terms("unique_activities").field("activityId")).size(10000).subAggregation(JoinAggregationBuilders.parent("activity_to_process", "activity")));
   }

   private FilterAggregationBuilder getFinishedActivitiesAgg() {
      QueryBuilder completedEndEventsQ = ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{QueryBuilders.termQuery("activityType", FlowNodeType.END_EVENT.toString()), QueryBuilders.termQuery("activityState", FlowNodeState.COMPLETED.toString())});
      return (FilterAggregationBuilder)AggregationBuilders.filter("finished_activities", completedEndEventsQ).subAggregation(((TermsAggregationBuilder)AggregationBuilders.terms("unique_activities").field("activityId")).size(10000).subAggregation(JoinAggregationBuilders.parent("activity_to_process", "activity")));
   }

   @FunctionalInterface
   private interface MapUpdater {
      void updateMapEntry(FlowNodeStatisticsDto var1, Long var2);
   }
}
