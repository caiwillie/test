/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  io.camunda.operate.entities.dmn.definition.DecisionDefinitionEntity
 *  io.camunda.operate.exceptions.OperateRuntimeException
 *  io.camunda.operate.schema.indices.DecisionIndex
 *  io.camunda.operate.schema.indices.DecisionRequirementsIndex
 *  io.camunda.operate.util.ElasticsearchUtil
 *  io.camunda.operate.webapp.es.reader.AbstractReader
 *  io.camunda.operate.webapp.rest.exception.NotFoundException
 *  org.elasticsearch.action.search.SearchRequest
 *  org.elasticsearch.action.search.SearchResponse
 *  org.elasticsearch.client.RequestOptions
 *  org.elasticsearch.index.query.QueryBuilder
 *  org.elasticsearch.index.query.QueryBuilders
 *  org.elasticsearch.search.SearchHit
 *  org.elasticsearch.search.aggregations.AbstractAggregationBuilder
 *  org.elasticsearch.search.aggregations.AggregationBuilder
 *  org.elasticsearch.search.aggregations.AggregationBuilders
 *  org.elasticsearch.search.aggregations.bucket.terms.Terms
 *  org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder
 *  org.elasticsearch.search.aggregations.metrics.TopHits
 *  org.elasticsearch.search.builder.SearchSourceBuilder
 *  org.elasticsearch.search.sort.SortOrder
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.es.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.entities.dmn.definition.DecisionDefinitionEntity;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.schema.indices.DecisionIndex;
import io.camunda.operate.schema.indices.DecisionRequirementsIndex;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.webapp.es.reader.AbstractReader;
import io.camunda.operate.webapp.rest.exception.NotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
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
public class DecisionReader
extends AbstractReader {
    private static final Logger logger = LoggerFactory.getLogger(DecisionReader.class);
    @Autowired
    private DecisionIndex decisionIndex;
    @Autowired
    private DecisionRequirementsIndex decisionRequirementsIndex;

    private DecisionDefinitionEntity fromSearchHit(String processString) {
        return (DecisionDefinitionEntity)ElasticsearchUtil.fromSearchHit((String)processString, (ObjectMapper)this.objectMapper, DecisionDefinitionEntity.class);
    }

    public String getDiagram(String decisionDefinitionId) {
        SearchRequest searchRequest = new SearchRequest(new String[]{this.decisionIndex.getAlias()}).source(new SearchSourceBuilder().query((QueryBuilder)QueryBuilders.idsQuery().addIds(new String[]{decisionDefinitionId})));
        try {
            SearchResponse response = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
            if (response.getHits().getTotalHits().value == 0L) {
                throw new NotFoundException("No decision definition found for id " + decisionDefinitionId);
            }
            Object key = response.getHits().getHits()[0].getSourceAsMap().get("decisionRequirementsKey");
            Long decisionRequirementsId = Long.valueOf(String.valueOf(key));
            searchRequest = new SearchRequest(new String[]{this.decisionRequirementsIndex.getAlias()}).source(new SearchSourceBuilder().query((QueryBuilder)QueryBuilders.idsQuery().addIds(new String[]{String.valueOf(decisionRequirementsId)})).fetchSource("xml", null));
            response = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
            if (response.getHits().getTotalHits().value == 1L) {
                Map result = response.getHits().getHits()[0].getSourceAsMap();
                return (String)result.get("xml");
            }
            if (response.getHits().getTotalHits().value <= 1L) throw new NotFoundException(String.format("Could not find DRD with id '%s'.", decisionRequirementsId));
            throw new NotFoundException(String.format("Could not find unique DRD with id '%s'.", decisionRequirementsId));
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining the decision diagram: %s", e.getMessage());
            logger.error(message, e);
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    public Map<String, List<DecisionDefinitionEntity>> getDecisionsGrouped() {
        String groupsAggName = "group_by_decisionId";
        String decisionsAggName = "decisions";
        AbstractAggregationBuilder agg = ((TermsAggregationBuilder)AggregationBuilders.terms((String)"group_by_decisionId").field("decisionId")).size(10000).subAggregation((AggregationBuilder)AggregationBuilders.topHits((String)"decisions").fetchSource(new String[]{"id", "name", "version", "decisionId"}, null).size(100).sort("version", SortOrder.DESC));
        SearchRequest searchRequest = new SearchRequest(new String[]{this.decisionIndex.getAlias()}).source(new SearchSourceBuilder().aggregation((AggregationBuilder)agg).size(0));
        try {
            SearchResponse searchResponse = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
            Terms groups = (Terms)searchResponse.getAggregations().get("group_by_decisionId");
            HashMap<String, List<DecisionDefinitionEntity>> result = new HashMap<String, List<DecisionDefinitionEntity>>();
            groups.getBuckets().stream().forEach(b -> {
                SearchHit[] hits;
                String decisionId = b.getKeyAsString();
                result.put(decisionId, new ArrayList());
                TopHits decisions = (TopHits)b.getAggregations().get("decisions");
                SearchHit[] searchHitArray = hits = decisions.getHits().getHits();
                int n = searchHitArray.length;
                int n2 = 0;
                while (n2 < n) {
                    SearchHit searchHit = searchHitArray[n2];
                    DecisionDefinitionEntity decisionEntity = this.fromSearchHit(searchHit.getSourceAsString());
                    ((List)result.get(decisionId)).add(decisionEntity);
                    ++n2;
                }
            });
            return result;
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining grouped processes: %s", e.getMessage());
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }
}
