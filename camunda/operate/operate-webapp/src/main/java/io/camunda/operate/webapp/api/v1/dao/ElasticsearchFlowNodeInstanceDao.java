/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.schema.templates.FlowNodeInstanceTemplate
 *  io.camunda.operate.util.ElasticsearchUtil
 *  io.camunda.operate.webapp.api.v1.dao.ElasticsearchDao
 *  io.camunda.operate.webapp.api.v1.dao.FlowNodeInstanceDao
 *  io.camunda.operate.webapp.api.v1.entities.FlowNodeInstance
 *  io.camunda.operate.webapp.api.v1.entities.Query
 *  io.camunda.operate.webapp.api.v1.entities.Results
 *  io.camunda.operate.webapp.api.v1.exceptions.APIException
 *  io.camunda.operate.webapp.api.v1.exceptions.ResourceNotFoundException
 *  io.camunda.operate.webapp.api.v1.exceptions.ServerException
 *  org.elasticsearch.action.search.SearchRequest
 *  org.elasticsearch.action.search.SearchResponse
 *  org.elasticsearch.client.RequestOptions
 *  org.elasticsearch.index.query.QueryBuilder
 *  org.elasticsearch.index.query.QueryBuilders
 *  org.elasticsearch.search.SearchHit
 *  org.elasticsearch.search.SearchHits
 *  org.elasticsearch.search.builder.SearchSourceBuilder
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.api.v1.dao;

import io.camunda.operate.schema.templates.FlowNodeInstanceTemplate;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.webapp.api.v1.dao.ElasticsearchDao;
import io.camunda.operate.webapp.api.v1.dao.FlowNodeInstanceDao;
import io.camunda.operate.webapp.api.v1.entities.FlowNodeInstance;
import io.camunda.operate.webapp.api.v1.entities.Query;
import io.camunda.operate.webapp.api.v1.entities.Results;
import io.camunda.operate.webapp.api.v1.exceptions.APIException;
import io.camunda.operate.webapp.api.v1.exceptions.ResourceNotFoundException;
import io.camunda.operate.webapp.api.v1.exceptions.ServerException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(value="ElasticsearchFlowNodeInstanceDaoV1")
public class ElasticsearchFlowNodeInstanceDao
extends ElasticsearchDao<FlowNodeInstance>
implements FlowNodeInstanceDao {
    @Autowired
    private FlowNodeInstanceTemplate flowNodeInstanceIndex;

    protected void buildFiltering(Query<FlowNodeInstance> query, SearchSourceBuilder searchSourceBuilder) {
        FlowNodeInstance filter = (FlowNodeInstance)query.getFilter();
        ArrayList<QueryBuilder> queryBuilders = new ArrayList<QueryBuilder>();
        if (filter != null) {
            queryBuilders.add(this.buildTermQuery("key", filter.getKey()));
            queryBuilders.add(this.buildTermQuery("processInstanceKey", filter.getProcessInstanceKey()));
            queryBuilders.add(this.buildMatchDateQuery("startDate", filter.getStartDate()));
            queryBuilders.add(this.buildMatchDateQuery("endDate", filter.getEndDate()));
            queryBuilders.add(this.buildTermQuery("state", filter.getState()));
            queryBuilders.add(this.buildTermQuery("type", filter.getType()));
            queryBuilders.add(this.buildTermQuery("incident", filter.getIncident()));
            queryBuilders.add(this.buildTermQuery("incidentKey", filter.getIncidentKey()));
        }
        searchSourceBuilder.query(ElasticsearchUtil.joinWithAnd((QueryBuilder[])queryBuilders.toArray(new QueryBuilder[0])));
    }

    public FlowNodeInstance byKey(Long key) throws APIException {
        List<FlowNodeInstance> flowNodeInstances;
        this.logger.debug("byKey {}", (Object)key);
        try {
            flowNodeInstances = this.searchFor(new SearchSourceBuilder().query((QueryBuilder)QueryBuilders.termQuery((String)"key", (Object)key)));
        }
        catch (Exception e) {
            throw new ServerException(String.format("Error in reading flownode instance for key %s", key), (Throwable)e);
        }
        if (flowNodeInstances.isEmpty()) {
            throw new ResourceNotFoundException(String.format("No flownode instance found for key %s ", key));
        }
        if (flowNodeInstances.size() <= 1) return flowNodeInstances.get(0);
        throw new ServerException(String.format("Found more than one flownode instances for key %s", key));
    }

    public Results<FlowNodeInstance> search(Query<FlowNodeInstance> query) throws APIException {
        this.logger.debug("search {}", (Object)query);
        SearchSourceBuilder searchSourceBuilder = this.buildQueryOn(query, "key", new SearchSourceBuilder());
        try {
            SearchRequest searchRequest = new SearchRequest().indices(new String[]{this.flowNodeInstanceIndex.getAlias()}).source(searchSourceBuilder);
            SearchResponse searchResponse = this.elasticsearch.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits searchHits = searchResponse.getHits();
            SearchHit[] searchHitArray = searchHits.getHits();
            if (searchHitArray == null) return new Results().setTotal(searchHits.getTotalHits().value);
            if (searchHitArray.length <= 0) return new Results().setTotal(searchHits.getTotalHits().value);
            Object[] sortValues = searchHitArray[searchHitArray.length - 1].getSortValues();
            List flowNodeInstances = ElasticsearchUtil.mapSearchHits((SearchHit[])searchHitArray, this::searchHitToIncident);
            return new Results().setTotal(searchHits.getTotalHits().value).setItems(flowNodeInstances).setSortValues(sortValues);
        }
        catch (Exception e) {
            throw new ServerException("Error in reading flownode instances", (Throwable)e);
        }
    }

    private FlowNodeInstance searchHitToIncident(SearchHit searchHit) {
        Map searchHitAsMap = searchHit.getSourceAsMap();
        return new FlowNodeInstance().setKey((Long)searchHitAsMap.get("key")).setProcessInstanceKey((Long)searchHitAsMap.get("processInstanceKey")).setStartDate((String)searchHitAsMap.get("startDate")).setEndDate((String)searchHitAsMap.get("endDate")).setType((String)searchHitAsMap.get("type")).setState((String)searchHitAsMap.get("state")).setIncident((Boolean)searchHitAsMap.get("incident")).setIncidentKey((Long)searchHitAsMap.get("incidentKey"));
    }

    protected List<FlowNodeInstance> searchFor(SearchSourceBuilder searchSourceBuilder) {
        try {
            SearchRequest searchRequest = new SearchRequest(new String[]{this.flowNodeInstanceIndex.getAlias()}).source(searchSourceBuilder);
            SearchResponse searchResponse = this.elasticsearch.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits searchHits = searchResponse.getHits();
            SearchHit[] searchHitArray = searchHits.getHits();
            if (searchHitArray == null) return List.of();
            if (searchHitArray.length <= 0) return List.of();
            return ElasticsearchUtil.mapSearchHits((SearchHit[])searchHitArray, this::searchHitToIncident);
        }
        catch (Exception e) {
            throw new ServerException("Error in reading incidents", (Throwable)e);
        }
    }
}
