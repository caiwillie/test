/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  io.camunda.operate.schema.templates.ListViewTemplate
 *  io.camunda.operate.util.ElasticsearchUtil
 *  io.camunda.operate.webapp.api.v1.dao.ElasticsearchDao
 *  io.camunda.operate.webapp.api.v1.dao.ProcessInstanceDao
 *  io.camunda.operate.webapp.api.v1.entities.ChangeStatus
 *  io.camunda.operate.webapp.api.v1.entities.ProcessInstance
 *  io.camunda.operate.webapp.api.v1.entities.Query
 *  io.camunda.operate.webapp.api.v1.entities.Results
 *  io.camunda.operate.webapp.api.v1.exceptions.APIException
 *  io.camunda.operate.webapp.api.v1.exceptions.ClientException
 *  io.camunda.operate.webapp.api.v1.exceptions.ResourceNotFoundException
 *  io.camunda.operate.webapp.api.v1.exceptions.ServerException
 *  io.camunda.operate.webapp.es.writer.ProcessInstanceWriter
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
import io.camunda.operate.schema.templates.ListViewTemplate;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.webapp.api.v1.dao.ElasticsearchDao;
import io.camunda.operate.webapp.api.v1.dao.ProcessInstanceDao;
import io.camunda.operate.webapp.api.v1.entities.ChangeStatus;
import io.camunda.operate.webapp.api.v1.entities.ProcessInstance;
import io.camunda.operate.webapp.api.v1.entities.Query;
import io.camunda.operate.webapp.api.v1.entities.Results;
import io.camunda.operate.webapp.api.v1.exceptions.APIException;
import io.camunda.operate.webapp.api.v1.exceptions.ClientException;
import io.camunda.operate.webapp.api.v1.exceptions.ResourceNotFoundException;
import io.camunda.operate.webapp.api.v1.exceptions.ServerException;
import io.camunda.operate.webapp.es.writer.ProcessInstanceWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

@Component(value="ElasticsearchProcessInstanceDaoV1")
public class ElasticsearchProcessInstanceDao
extends ElasticsearchDao<ProcessInstance>
implements ProcessInstanceDao {
    @Autowired
    private ListViewTemplate processInstanceIndex;
    @Autowired
    private ProcessInstanceWriter processInstanceWriter;

    public Results<ProcessInstance> search(Query<ProcessInstance> query) throws APIException {
        this.logger.debug("search {}", (Object)query);
        SearchSourceBuilder searchSourceBuilder = this.buildQueryOn(query, "processInstanceKey", new SearchSourceBuilder());
        try {
            SearchRequest searchRequest = new SearchRequest().indices(new String[]{this.processInstanceIndex.getAlias()}).source(searchSourceBuilder);
            SearchResponse searchResponse = this.elasticsearch.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits searchHits = searchResponse.getHits();
            SearchHit[] searchHitArray = searchHits.getHits();
            if (searchHitArray == null) return new Results().setTotal(searchHits.getTotalHits().value);
            if (searchHitArray.length <= 0) return new Results().setTotal(searchHits.getTotalHits().value);
            Object[] sortValues = searchHitArray[searchHitArray.length - 1].getSortValues();
            List processInstances = ElasticsearchUtil.mapSearchHits((SearchHit[])searchHitArray, (ObjectMapper)this.objectMapper, ProcessInstance.class);
            return new Results().setTotal(searchHits.getTotalHits().value).setItems(processInstances).setSortValues(sortValues);
        }
        catch (Exception e) {
            throw new ServerException("Error in reading process instances", (Throwable)e);
        }
    }

    public ProcessInstance byKey(Long key) throws APIException {
        List<ProcessInstance> processInstances;
        this.logger.debug("byKey {}", (Object)key);
        try {
            processInstances = this.searchFor(new SearchSourceBuilder().query((QueryBuilder)QueryBuilders.termQuery((String)"key", (Object)key)));
        }
        catch (Exception e) {
            throw new ServerException(String.format("Error in reading process instance for key %s", key), (Throwable)e);
        }
        if (processInstances.isEmpty()) {
            throw new ResourceNotFoundException(String.format("No process instances found for key %s ", key));
        }
        if (processInstances.size() <= 1) return processInstances.get(0);
        throw new ServerException(String.format("Found more than one process instances for key %s", key));
    }

    public ChangeStatus delete(Long key) throws APIException {
        this.byKey(key);
        try {
            this.processInstanceWriter.deleteInstanceById(key);
            return new ChangeStatus().setDeleted(1L).setMessage(String.format("Process instance and dependant data deleted for key '%s'", key));
        }
        catch (IllegalArgumentException iae) {
            throw new ClientException(iae.getMessage(), (Throwable)iae);
        }
        catch (Exception e) {
            throw new ServerException(String.format("Error in deleting process instance and dependant data for key '%s'", key), (Throwable)e);
        }
    }

    protected void buildFiltering(Query<ProcessInstance> query, SearchSourceBuilder searchSourceBuilder) {
        ProcessInstance filter = (ProcessInstance)query.getFilter();
        ArrayList<Object> queryBuilders = new ArrayList<Object>();
        queryBuilders.add(QueryBuilders.termQuery((String)"joinRelation", (String)"processInstance"));
        if (filter != null) {
            queryBuilders.add(this.buildTermQuery("processInstanceKey", filter.getKey()));
            queryBuilders.add(this.buildTermQuery("processDefinitionKey", filter.getProcessDefinitionKey()));
            queryBuilders.add(this.buildTermQuery("parentProcessInstanceKey", filter.getParentKey()));
            queryBuilders.add(this.buildTermQuery("processVersion", filter.getProcessVersion()));
            queryBuilders.add(this.buildTermQuery("bpmnProcessId", filter.getBpmnProcessId()));
            queryBuilders.add(this.buildTermQuery("state", filter.getState()));
            queryBuilders.add(this.buildMatchDateQuery("startDate", filter.getStartDate()));
            queryBuilders.add(this.buildMatchDateQuery("endDate", filter.getEndDate()));
        }
        searchSourceBuilder.query(ElasticsearchUtil.joinWithAnd((QueryBuilder[])queryBuilders.toArray(new QueryBuilder[0])));
    }

    protected List<ProcessInstance> searchFor(SearchSourceBuilder searchSource) throws IOException {
        SearchRequest searchRequest = new SearchRequest(new String[]{this.processInstanceIndex.getAlias()}).source(searchSource);
        return ElasticsearchUtil.scroll((SearchRequest)searchRequest, ProcessInstance.class, (ObjectMapper)this.objectMapper, (RestHighLevelClient)this.elasticsearch);
    }
}
