package com.brandnewdata.mop.poc.operate.dao;

import cn.hutool.core.map.MapUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.brandnewdata.mop.poc.operate.po.VariablePo;
import com.brandnewdata.mop.poc.operate.schema.template.VariableTemplate;
import com.brandnewdata.mop.poc.operate.util.ElasticsearchUtil;

import java.util.List;
import java.util.Map;

public class VariableDao {

    private static final VariableTemplate TEMPLATE = new VariableTemplate();

    private static final Map<ElasticsearchClient, VariableDao> INSTANCE_MAP = MapUtil.newConcurrentHashMap();

    private final ElasticsearchClient client;

    private VariableDao(ElasticsearchClient client) {
        this.client = client;
    }

    public static VariableDao getInstance(ElasticsearchClient client) {
        return INSTANCE_MAP.computeIfAbsent(client, VariableDao::new);
    }

    public List<VariablePo> list(Query query) {
        SearchRequest request = new SearchRequest.Builder()
                .index(TEMPLATE.getAlias())
                .query(query)
                .build();
        return ElasticsearchUtil.scrollAll(client, request, VariablePo.class);
    }

}
