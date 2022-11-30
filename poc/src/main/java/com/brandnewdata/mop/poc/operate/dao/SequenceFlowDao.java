package com.brandnewdata.mop.poc.operate.dao;

import cn.hutool.core.map.MapUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.brandnewdata.mop.poc.operate.po.SequenceFlowPo;
import com.brandnewdata.mop.poc.operate.schema.template.SequenceFlowTemplate;
import com.brandnewdata.mop.poc.operate.util.ElasticsearchUtil;

import java.util.List;
import java.util.Map;

public class SequenceFlowDao {
    private static final SequenceFlowTemplate TEMPLATE = new SequenceFlowTemplate();

    private static final Map<ElasticsearchClient, SequenceFlowDao> INSTANCE_MAP = MapUtil.newConcurrentHashMap();

    private final ElasticsearchClient client;

    private SequenceFlowDao(ElasticsearchClient client) {
        this.client = client;
    }

    public static SequenceFlowDao getInstance(ElasticsearchClient client) {
        return INSTANCE_MAP.computeIfAbsent(client, SequenceFlowDao::new);
    }

    public List<SequenceFlowPo> scrollAll(Query query) {
        SearchRequest request = new SearchRequest.Builder()
                .index(TEMPLATE.getAlias())
                .query(query)
                .build();
        return ElasticsearchUtil.scrollAll(client, request, SequenceFlowPo.class);
    }

}
