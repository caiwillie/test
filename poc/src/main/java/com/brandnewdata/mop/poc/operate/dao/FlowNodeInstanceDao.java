package com.brandnewdata.mop.poc.operate.dao;

import cn.hutool.core.lang.Assert;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.brandnewdata.mop.poc.operate.entity.FlowNodeInstanceEntity;
import com.brandnewdata.mop.poc.operate.schema.template.FlowNodeInstanceTemplate;
import com.brandnewdata.mop.poc.operate.util.ElasticsearchUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FlowNodeInstanceDao extends AbstractDao {

    @Autowired
    private FlowNodeInstanceTemplate template;

    public List<FlowNodeInstanceEntity> list(String processInstanceId) {
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(template.getAlias())
                .query(new Query.Builder()
                        .term(t -> t.field(FlowNodeInstanceTemplate.PROCESS_INSTANCE_KEY).value(processInstanceId))
                        .build())
                .build();
        return ElasticsearchUtil.scrollAll(client, searchRequest, FlowNodeInstanceEntity.class);
    }

    public List<FlowNodeInstanceEntity> list(Query query, ElasticsearchUtil.QueryType queryType) {
        Assert.notNull(query);
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(ElasticsearchUtil.whereToSearch(template, queryType))
                .query(query)
                .build();
        return ElasticsearchUtil.scrollAll(client, searchRequest, FlowNodeInstanceEntity.class);
    }

    public void getFlowNodeMetadata() {

    }

    public void getFlowNodeStates() {

    }

    public FlowNodeInstanceEntity searchOne(String flowNodeInstanceId) {
        Query query = new Query.Builder()
                .term(t -> t.field(FlowNodeInstanceTemplate.ID).value(flowNodeInstanceId))
                .build();

        SearchRequest request = new SearchRequest.Builder()
                .index(template.getAlias())
                .query(query)
                .build();
        
        return ElasticsearchUtil.searchOne(client, request, FlowNodeInstanceEntity.class);
    }

}
