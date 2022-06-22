/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.schema.templates.VariableTemplate
 *  io.camunda.operate.util.ElasticsearchUtil
 *  io.camunda.operate.webapp.api.v1.dao.ElasticsearchDao
 *  io.camunda.operate.webapp.api.v1.dao.VariableDao
 *  io.camunda.operate.webapp.api.v1.entities.Query
 *  io.camunda.operate.webapp.api.v1.entities.Results
 *  io.camunda.operate.webapp.api.v1.entities.Variable
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

import io.camunda.operate.schema.templates.VariableTemplate;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.webapp.api.v1.dao.ElasticsearchDao;
import io.camunda.operate.webapp.api.v1.dao.VariableDao;
import io.camunda.operate.webapp.api.v1.entities.Query;
import io.camunda.operate.webapp.api.v1.entities.Results;
import io.camunda.operate.webapp.api.v1.entities.Variable;
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

@Component(value="ElasticsearchVariableDaoV1")
public class ElasticsearchVariableDao
extends ElasticsearchDao<Variable>
implements VariableDao {
    @Autowired
    private VariableTemplate variableIndex;

    protected void buildFiltering(Query<Variable> query, SearchSourceBuilder searchSourceBuilder) {
        Variable filter = (Variable)query.getFilter();
        ArrayList<QueryBuilder> queryBuilders = new ArrayList<QueryBuilder>();
        if (filter != null) {
            queryBuilders.add(this.buildTermQuery("key", filter.getKey()));
            queryBuilders.add(this.buildTermQuery("processInstanceKey", filter.getProcessInstanceKey()));
            queryBuilders.add(this.buildTermQuery("scopeKey", filter.getScopeKey()));
            queryBuilders.add(this.buildTermQuery("name", filter.getName()));
            queryBuilders.add(this.buildTermQuery("value", filter.getValue()));
            queryBuilders.add(this.buildTermQuery("isPreview", filter.getTruncated()));
        }
        searchSourceBuilder.query(ElasticsearchUtil.joinWithAnd((QueryBuilder[])queryBuilders.toArray(new QueryBuilder[0])));
    }

    public Variable byKey(Long key) throws APIException {
        List<Variable> variables;
        this.logger.debug("byKey {}", (Object)key);
        try {
            variables = this.searchFor(new SearchSourceBuilder().query((QueryBuilder)QueryBuilders.termQuery((String)"key", (Object)key)));
        }
        catch (Exception e) {
            throw new ServerException(String.format("Error in reading variable for key %s", key), (Throwable)e);
        }
        if (variables.isEmpty()) {
            throw new ResourceNotFoundException(String.format("No variable found for key %s ", key));
        }
        if (variables.size() <= 1) return variables.get(0);
        throw new ServerException(String.format("Found more than one variables for key %s", key));
    }

    public Results<Variable> search(Query<Variable> query) throws APIException {
        this.logger.debug("search {}", (Object)query);
        SearchSourceBuilder searchSourceBuilder = this.buildQueryOn(query, "key", new SearchSourceBuilder());
        try {
            SearchRequest searchRequest = new SearchRequest().indices(new String[]{this.variableIndex.getAlias()}).source(searchSourceBuilder);
            SearchResponse searchResponse = this.elasticsearch.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits searchHits = searchResponse.getHits();
            SearchHit[] searchHitArray = searchHits.getHits();
            if (searchHitArray == null) return new Results().setTotal(searchHits.getTotalHits().value);
            if (searchHitArray.length <= 0) return new Results().setTotal(searchHits.getTotalHits().value);
            Object[] sortValues = searchHitArray[searchHitArray.length - 1].getSortValues();
            List variables = ElasticsearchUtil.mapSearchHits((SearchHit[])searchHitArray, this::searchHitToVariableWithoutFullValue);
            return new Results().setTotal(searchHits.getTotalHits().value).setItems(variables).setSortValues(sortValues);
        }
        catch (Exception e) {
            throw new ServerException("Error in reading incidents", (Throwable)e);
        }
    }

    protected Variable searchHitToVariableWithoutFullValue(SearchHit searchHit) {
        return this.searchHitToVariable(searchHit, false);
    }

    protected Variable searchHitToVariableWithFullValue(SearchHit searchHit) {
        return this.searchHitToVariable(searchHit, true);
    }

    protected Variable searchHitToVariable(SearchHit searchHit, boolean isFullValue) {
        Map searchHitAsMap = searchHit.getSourceAsMap();
        Variable variable = new Variable().setKey((Long)searchHitAsMap.get("key")).setProcessInstanceKey((Long)searchHitAsMap.get("processInstanceKey")).setScopeKey((Long)searchHitAsMap.get("scopeKey")).setName((String)searchHitAsMap.get("name")).setValue((String)searchHitAsMap.get("value")).setTruncated((Boolean)searchHitAsMap.get("isPreview"));
        if (!isFullValue) return variable;
        String fullValue = (String)searchHitAsMap.get("fullValue");
        if (fullValue != null) {
            variable.setValue(fullValue);
        }
        variable.setTruncated(Boolean.valueOf(false));
        return variable;
    }

    protected List<Variable> searchFor(SearchSourceBuilder searchSourceBuilder) {
        try {
            SearchRequest searchRequest = new SearchRequest(new String[]{this.variableIndex.getAlias()}).source(searchSourceBuilder);
            SearchResponse searchResponse = this.elasticsearch.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits searchHits = searchResponse.getHits();
            SearchHit[] searchHitArray = searchHits.getHits();
            if (searchHitArray == null) return List.of();
            if (searchHitArray.length <= 0) return List.of();
            return ElasticsearchUtil.mapSearchHits((SearchHit[])searchHitArray, this::searchHitToVariableWithFullValue);
        }
        catch (Exception e) {
            throw new ServerException("Error in reading variables", (Throwable)e);
        }
    }
}
