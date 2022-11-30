package com.brandnewdata.mop.poc.operate.dao;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.brandnewdata.mop.poc.operate.entity.FlowNodeInstanceEntity;
import com.brandnewdata.mop.poc.operate.schema.template.FlowNodeInstanceTemplate;
import com.brandnewdata.mop.poc.operate.util.ElasticsearchUtil;

import java.util.List;
import java.util.Map;

public class FlowNodeInstanceDao {
    private static final FlowNodeInstanceTemplate TEMPLATE = new FlowNodeInstanceTemplate();

    private static final Map<ElasticsearchClient, FlowNodeInstanceDao> INSTANCE_MAP = MapUtil.newConcurrentHashMap();

    private final ElasticsearchClient client;

    private FlowNodeInstanceDao(ElasticsearchClient client) {
        this.client = client;
    }

    public static FlowNodeInstanceDao getInstance(ElasticsearchClient client) {
        return INSTANCE_MAP.computeIfAbsent(client, FlowNodeInstanceDao::new);
    }

    public List<FlowNodeInstanceEntity> list(String processInstanceId) {
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(TEMPLATE.getAlias())
                .query(new Query.Builder()
                        .term(t -> t.field(FlowNodeInstanceTemplate.PROCESS_INSTANCE_KEY).value(processInstanceId))
                        .build())
                .build();
        return ElasticsearchUtil.scrollAll(client, searchRequest, FlowNodeInstanceEntity.class);
    }

    public List<FlowNodeInstanceEntity> list(Query query, ElasticsearchUtil.QueryType queryType) {
        Assert.notNull(query);
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(ElasticsearchUtil.whereToSearch(TEMPLATE, queryType))
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
                .index(TEMPLATE.getAlias())
                .query(query)
                .build();
        
        return ElasticsearchUtil.searchOne(client, request, FlowNodeInstanceEntity.class);
    }

}
