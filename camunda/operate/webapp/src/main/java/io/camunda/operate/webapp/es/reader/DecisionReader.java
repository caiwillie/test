package io.camunda.operate.webapp.es.reader;

import io.camunda.operate.entities.dmn.definition.DecisionDefinitionEntity;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.schema.indices.DecisionIndex;
import io.camunda.operate.schema.indices.DecisionRequirementsIndex;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.webapp.rest.exception.NotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
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
public class DecisionReader extends AbstractReader {
   private static final Logger logger = LoggerFactory.getLogger(DecisionReader.class);
   @Autowired
   private DecisionIndex decisionIndex;
   @Autowired
   private DecisionRequirementsIndex decisionRequirementsIndex;

   private DecisionDefinitionEntity fromSearchHit(String processString) {
      return (DecisionDefinitionEntity)ElasticsearchUtil.fromSearchHit(processString, this.objectMapper, DecisionDefinitionEntity.class);
   }

   public String getDiagram(String decisionDefinitionId) {
      SearchRequest searchRequest = (new SearchRequest(new String[]{this.decisionIndex.getAlias()})).source((new SearchSourceBuilder()).query(QueryBuilders.idsQuery().addIds(new String[]{decisionDefinitionId})));

      try {
         SearchResponse response = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
         if (response.getHits().getTotalHits().value == 0L) {
            throw new NotFoundException("No decision definition found for id " + decisionDefinitionId);
         } else {
            Object key = response.getHits().getHits()[0].getSourceAsMap().get("decisionRequirementsKey");
            Long decisionRequirementsId = Long.valueOf(String.valueOf(key));
            searchRequest = (new SearchRequest(new String[]{this.decisionRequirementsIndex.getAlias()})).source((new SearchSourceBuilder()).query(QueryBuilders.idsQuery().addIds(new String[]{String.valueOf(decisionRequirementsId)})).fetchSource("xml", (String)null));
            response = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
            if (response.getHits().getTotalHits().value == 1L) {
               Map result = response.getHits().getHits()[0].getSourceAsMap();
               return (String)result.get("xml");
            } else if (response.getHits().getTotalHits().value > 1L) {
               throw new NotFoundException(String.format("Could not find unique DRD with id '%s'.", decisionRequirementsId));
            } else {
               throw new NotFoundException(String.format("Could not find DRD with id '%s'.", decisionRequirementsId));
            }
         }
      } catch (IOException var7) {
         String message = String.format("Exception occurred, while obtaining the decision diagram: %s", var7.getMessage());
         logger.error(message, var7);
         throw new OperateRuntimeException(message, var7);
      }
   }

   public Map getDecisionsGrouped() {
      String groupsAggName = "group_by_decisionId";
      String decisionsAggName = "decisions";
      AggregationBuilder agg = ((TermsAggregationBuilder)AggregationBuilders.terms("group_by_decisionId").field("decisionId")).size(10000).subAggregation(AggregationBuilders.topHits("decisions").fetchSource(new String[]{"id", "name", "version", "decisionId"}, (String[])null).size(100).sort("version", SortOrder.DESC));
      SearchRequest searchRequest = (new SearchRequest(new String[]{this.decisionIndex.getAlias()})).source((new SearchSourceBuilder()).aggregation(agg).size(0));

      try {
         SearchResponse searchResponse = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
         Terms groups = (Terms)searchResponse.getAggregations().get("group_by_decisionId");
         Map result = new HashMap();
         groups.getBuckets().stream().forEach((b) -> {
            String decisionId = b.getKeyAsString();
            result.put(decisionId, new ArrayList());
            TopHits decisions = (TopHits)b.getAggregations().get("decisions");
            SearchHit[] hits = decisions.getHits().getHits();
            SearchHit[] var6 = hits;
            int var7 = hits.length;

            for(int var8 = 0; var8 < var7; ++var8) {
               SearchHit searchHit = var6[var8];
               DecisionDefinitionEntity decisionEntity = this.fromSearchHit(searchHit.getSourceAsString());
               ((List)result.get(decisionId)).add(decisionEntity);
            }

         });
         return result;
      } catch (IOException var8) {
         String message = String.format("Exception occurred, while obtaining grouped processes: %s", var8.getMessage());
         throw new OperateRuntimeException(message, var8);
      }
   }
}
