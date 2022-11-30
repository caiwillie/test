package com.brandnewdata.mop.poc.operate.dao;

import cn.hutool.core.map.MapUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.brandnewdata.mop.poc.operate.po.EventPo;
import com.brandnewdata.mop.poc.operate.schema.template.EventTemplate;
import com.brandnewdata.mop.poc.operate.util.ElasticsearchUtil;
import lombok.SneakyThrows;

import java.util.Map;

public class EventDao {
    private static final EventTemplate TEMPLATE = new EventTemplate();

    private static final Map<ElasticsearchClient, EventDao> INSTANCE_MAP = MapUtil.newConcurrentHashMap();

    private final ElasticsearchClient client;

    private EventDao(ElasticsearchClient client) {
        this.client = client;
    }

    public static EventDao getInstance(ElasticsearchClient client) {
        return INSTANCE_MAP.computeIfAbsent(client, EventDao::new);
    }

    @SneakyThrows
    public EventPo getOne(String flowNodeInstanceId) {
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(TEMPLATE.getAlias())
                .query(new Query.Builder()
                        .term(t -> t.field(EventTemplate.FLOW_NODE_INSTANCE_KEY).value(flowNodeInstanceId))
                        .build()
                )
                .build();

        return ElasticsearchUtil.searchOne(client, searchRequest, EventPo.class);
    }

}
