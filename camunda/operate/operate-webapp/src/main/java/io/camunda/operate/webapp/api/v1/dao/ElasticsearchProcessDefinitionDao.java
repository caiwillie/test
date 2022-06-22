/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  io.camunda.operate.schema.indices.ProcessIndex
 *  io.camunda.operate.util.ElasticsearchUtil
 *  io.camunda.operate.webapp.api.v1.dao.ElasticsearchDao
 *  io.camunda.operate.webapp.api.v1.dao.ProcessDefinitionDao
 *  io.camunda.operate.webapp.api.v1.entities.ProcessDefinition
 *  io.camunda.operate.webapp.api.v1.entities.Query
 *  io.camunda.operate.webapp.api.v1.entities.Results
 *  io.camunda.operate.webapp.api.v1.exceptions.APIException
 *  io.camunda.operate.webapp.api.v1.exceptions.ResourceNotFoundException
 *  io.camunda.operate.webapp.api.v1.exceptions.ServerException
 *  org.elasticsearch.action.search.SearchRequest
 *  org.elasticsearch.action.search.SearchResponse
 *  org.elasticsearch.client.RequestOptions
 *  org.elasticsearch.client.RestHighLevelClient
 *  org.elasticsearch.index.query.QueryBuilder
 *  org.elasticsearch.index.query.QueryBuilders
 *  org.elasticsearch.search.SearchHit
 *  org.elasticsearch.search.SearchHits
 *  org.elasticsearch.search.builder.SearchSourceBuilder
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.api.v1.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.schema.indices.ProcessIndex;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.webapp.api.v1.dao.ElasticsearchDao;
import io.camunda.operate.webapp.api.v1.dao.ProcessDefinitionDao;
import io.camunda.operate.webapp.api.v1.entities.ProcessDefinition;
import io.camunda.operate.webapp.api.v1.entities.Query;
import io.camunda.operate.webapp.api.v1.entities.Results;
import io.camunda.operate.webapp.api.v1.exceptions.APIException;
import io.camunda.operate.webapp.api.v1.exceptions.ResourceNotFoundException;
import io.camunda.operate.webapp.api.v1.exceptions.ServerException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(value="ElasticsearchProcessDefinitionDaoV1")
public class ElasticsearchProcessDefinitionDao
extends ElasticsearchDao<ProcessDefinition>
implements ProcessDefinitionDao {
    @Autowired
    private ProcessIndex processIndex;

    public Results<ProcessDefinition> search(Query<ProcessDefinition> query) throws APIException {
        this.logger.debug("search {}", (Object)query);
        SearchSourceBuilder searchSourceBuilder = this.buildQueryOn(query, "key", new SearchSourceBuilder());
        try {
            SearchRequest searchRequest = new SearchRequest().indices(new String[]{this.processIndex.getAlias()}).source(searchSourceBuilder);
            SearchResponse searchResponse = this.elasticsearch.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits searchHits = searchResponse.getHits();
            SearchHit[] searchHitArray = searchHits.getHits();
            if (searchHitArray == null) return new Results().setTotal(searchHits.getTotalHits().value);
            if (searchHitArray.length <= 0) return new Results().setTotal(searchHits.getTotalHits().value);
            Object[] sortValues = searchHitArray[searchHitArray.length - 1].getSortValues();
            return new Results().setTotal(searchHits.getTotalHits().value).setItems(ElasticsearchUtil.mapSearchHits((SearchHit[])searchHitArray, (ObjectMapper)this.objectMapper, ProcessDefinition.class)).setSortValues(sortValues);
        }
        catch (Exception e) {
            throw new ServerException("Error in reading process definitions", (Throwable)e);
        }
    }

    public ProcessDefinition byKey(Long key) throws APIException {
        List<ProcessDefinition> processDefinitions;
        this.logger.debug("byKey {}", (Object)key);
        try {
            processDefinitions = this.searchFor(new SearchSourceBuilder().query((QueryBuilder)QueryBuilders.termQuery((String)"key", (Object)key)));
        }
        catch (Exception e) {
            throw new ServerException(String.format("Error in reading process definition for key %s", key), (Throwable)e);
        }
        if (processDefinitions.isEmpty()) {
            throw new ResourceNotFoundException(String.format("No process definition found for key %s ", key));
        }
        if (processDefinitions.size() <= 1) return processDefinitions.get(0);
        throw new ServerException(String.format("Found more than one process definition for key %s", key));
    }

    public String xmlByKey(Long key) throws APIException {
        try {
            SearchRequest searchRequest = new SearchRequest(new String[]{this.processIndex.getAlias()}).source(new SearchSourceBuilder().query((QueryBuilder)QueryBuilders.termQuery((String)"key", (Object)key)).fetchSource("bpmnXml", null));
            SearchResponse response = this.elasticsearch.search(searchRequest, RequestOptions.DEFAULT);
            if (response.getHits().getTotalHits().value != 1L) throw new ResourceNotFoundException(String.format("Process definition for key %s not found.", key));
            Map result = response.getHits().getHits()[0].getSourceAsMap();
            return (String)result.get("bpmnXml");
        }
        catch (IOException e) {
            throw new ServerException(String.format("Error in reading process definition as xml for key %s", key), (Throwable)e);
        }
    }

    protected void buildFiltering(Query<ProcessDefinition> query, SearchSourceBuilder searchSourceBuilder) {
        ProcessDefinition filter = (ProcessDefinition)query.getFilter();
        if (filter == null) return;
        ArrayList<QueryBuilder> queryBuilders = new ArrayList<QueryBuilder>();
        queryBuilders.add(this.buildTermQuery("name", filter.getName()));
        queryBuilders.add(this.buildTermQuery("bpmnProcessId", filter.getBpmnProcessId()));
        queryBuilders.add(this.buildTermQuery("version", filter.getVersion()));
        queryBuilders.add(this.buildTermQuery("key", filter.getKey()));
        searchSourceBuilder.query(ElasticsearchUtil.joinWithAnd((QueryBuilder[])queryBuilders.toArray(new QueryBuilder[0])));
    }

    protected List<ProcessDefinition> searchFor(SearchSourceBuilder searchSource) throws IOException {
        SearchRequest searchRequest = new SearchRequest(new String[]{this.processIndex.getAlias()}).source(searchSource);
        return ElasticsearchUtil.scroll((SearchRequest)searchRequest, ProcessDefinition.class, (ObjectMapper)this.objectMapper, (RestHighLevelClient)this.elasticsearch);
    }
}
