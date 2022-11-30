package com.brandnewdata.mop.poc.operate.dao;

import cn.hutool.core.map.MapUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.brandnewdata.mop.poc.operate.entity.VariableEntity;
import com.brandnewdata.mop.poc.operate.schema.template.VariableTemplate;
import com.brandnewdata.mop.poc.operate.util.ElasticsearchUtil;

import java.util.List;
import java.util.Map;

public class VariableDao {

    private static final VariableTemplate TEMPLATE = new VariableTemplate();

    private static final Map<ElasticsearchClient, VariableDao> instanceMap = MapUtil.newConcurrentHashMap();

    private final ElasticsearchClient client;

    private VariableDao(ElasticsearchClient client) {
        this.client = client;
    }

    public static VariableDao getInstance(ElasticsearchClient client) {
        return instanceMap.computeIfAbsent(client, VariableDao::new);
    }

    public List<VariableEntity> list(Query query) {
        SearchRequest request = new SearchRequest.Builder()
                .index(TEMPLATE.getAlias())
                .query(query)
                .build();
        return ElasticsearchUtil.scrollAll(client, request, VariableEntity.class);
    }

}
