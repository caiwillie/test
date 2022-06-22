/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.schema.templates.IncidentTemplate
 *  io.camunda.operate.util.ElasticsearchUtil
 *  io.camunda.operate.webapp.api.v1.dao.ElasticsearchDao
 *  io.camunda.operate.webapp.api.v1.dao.IncidentDao
 *  io.camunda.operate.webapp.api.v1.entities.Incident
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

import io.camunda.operate.schema.templates.IncidentTemplate;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.webapp.api.v1.dao.ElasticsearchDao;
import io.camunda.operate.webapp.api.v1.dao.IncidentDao;
import io.camunda.operate.webapp.api.v1.entities.Incident;
import io.camunda.operate.webapp.api.v1.entities.Query;
import io.camunda.operate.webapp.api.v1.entities.Results;
import io.camunda.operate.webapp.api.v1.exceptions.APIException;
import io.camunda.operate.webapp.api.v1.exceptions.ResourceNotFoundException;
import io.camunda.operate.webapp.api.v1.exceptions.ServerException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

@Component(value="ElasticsearchIncidentDaoV1")
public class ElasticsearchIncidentDao
extends ElasticsearchDao<Incident>
implements IncidentDao {
    @Autowired
    private IncidentTemplate incidentIndex;

    protected void buildFiltering(Query<Incident> query, SearchSourceBuilder searchSourceBuilder) {
        Incident filter = (Incident)query.getFilter();
        ArrayList<QueryBuilder> queryBuilders = new ArrayList<QueryBuilder>();
        if (filter != null) {
            queryBuilders.add(this.buildTermQuery("key", filter.getKey()));
            queryBuilders.add(this.buildTermQuery("processDefinitionKey", filter.getProcessDefinitionKey()));
            queryBuilders.add(this.buildTermQuery("processInstanceKey", filter.getProcessInstanceKey()));
            queryBuilders.add(this.buildTermQuery("errorType", filter.getType()));
            queryBuilders.add(this.buildMatchQuery("errorMessage", filter.getMessage()));
            queryBuilders.add(this.buildTermQuery("state", filter.getState()));
            queryBuilders.add(this.buildMatchDateQuery("creationTime", filter.getCreationTime()));
        }
        searchSourceBuilder.query(ElasticsearchUtil.joinWithAnd((QueryBuilder[])queryBuilders.toArray(new QueryBuilder[0])));
    }

    public Incident byKey(Long key) throws APIException {
        List<Incident> incidents;
        this.logger.debug("byKey {}", (Object)key);
        try {
            incidents = this.searchFor(new SearchSourceBuilder().query((QueryBuilder)QueryBuilders.termQuery((String)"key", (Object)key)));
        }
        catch (Exception e) {
            throw new ServerException(String.format("Error in reading incident for key %s", key), (Throwable)e);
        }
        if (incidents.isEmpty()) {
            throw new ResourceNotFoundException(String.format("No incident found for key %s ", key));
        }
        if (incidents.size() <= 1) return incidents.get(0);
        throw new ServerException(String.format("Found more than one incidents for key %s", key));
    }

    public Results<Incident> search(Query<Incident> query) throws APIException {
        this.logger.debug("search {}", (Object)query);
        this.mapFieldsInSort(query);
        SearchSourceBuilder searchSourceBuilder = this.buildQueryOn(query, "key", new SearchSourceBuilder());
        try {
            SearchRequest searchRequest = new SearchRequest().indices(new String[]{this.incidentIndex.getAlias()}).source(searchSourceBuilder);
            SearchResponse searchResponse = this.elasticsearch.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits searchHits = searchResponse.getHits();
            SearchHit[] searchHitArray = searchHits.getHits();
            if (searchHitArray == null) return new Results().setTotal(searchHits.getTotalHits().value);
            if (searchHitArray.length <= 0) return new Results().setTotal(searchHits.getTotalHits().value);
            Object[] sortValues = searchHitArray[searchHitArray.length - 1].getSortValues();
            List incidents = ElasticsearchUtil.mapSearchHits((SearchHit[])searchHitArray, this::searchHitToIncident);
            return new Results().setTotal(searchHits.getTotalHits().value).setItems(incidents).setSortValues(sortValues);
        }
        catch (Exception e) {
            throw new ServerException("Error in reading incidents", (Throwable)e);
        }
    }

    private void mapFieldsInSort(Query<Incident> query) {
        if (query.getSort() == null) {
            return;
        }
        query.setSort(query.getSort().stream().map(s -> s.setField(Incident.OBJECT_TO_ELASTICSEARCH.getOrDefault(s.getField(), s.getField()))).collect(Collectors.toList()));
    }

    protected Incident searchHitToIncident(SearchHit searchHit) {
        Map searchHitAsMap = searchHit.getSourceAsMap();
        return new Incident().setKey((Long)searchHitAsMap.get("key")).setProcessInstanceKey((Long)searchHitAsMap.get("processInstanceKey")).setProcessDefinitionKey((Long)searchHitAsMap.get("processDefinitionKey")).setType((String)searchHitAsMap.get("errorType")).setMessage((String)searchHitAsMap.get("errorMessage")).setCreationTime((String)searchHitAsMap.get("creationTime")).setState((String)searchHitAsMap.get("state"));
    }

    protected List<Incident> searchFor(SearchSourceBuilder searchSourceBuilder) {
        try {
            SearchRequest searchRequest = new SearchRequest(new String[]{this.incidentIndex.getAlias()}).source(searchSourceBuilder);
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
