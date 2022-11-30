package com.brandnewdata.mop.poc.operate.dao;

import cn.hutool.core.map.MapUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.ConstantScoreQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.brandnewdata.mop.poc.operate.po.IncidentPo;
import com.brandnewdata.mop.poc.operate.schema.template.IncidentTemplate;
import com.brandnewdata.mop.poc.operate.util.ElasticsearchUtil;

import java.util.List;
import java.util.Map;

public class IncidentDao {
    private static final IncidentTemplate TEMPLATE = new IncidentTemplate();

    private static final Map<ElasticsearchClient, IncidentDao> INSTANCE_MAP = MapUtil.newConcurrentHashMap();

    private final ElasticsearchClient client;

    private IncidentDao(ElasticsearchClient client) {
        this.client = client;
    }

    public static IncidentDao getInstance(ElasticsearchClient client) {
        return INSTANCE_MAP.computeIfAbsent(client, IncidentDao::new);
    }

    public List<IncidentPo> listByTreePath(String treePath) {
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(TEMPLATE.getAlias())
                .query(new Query.Builder()
                        .constantScore(new ConstantScoreQuery.Builder()
                                .filter(new Query.Builder()
                                        .bool(new BoolQuery.Builder()
                                                .must(
                                                        new Query.Builder().term(t -> t.field(IncidentTemplate.TREE_PATH).value(treePath)).build(),
                                                        new Query.Builder().term(t -> t.field(IncidentTemplate.STATE).value("ACTIVE")).build()
                                                )
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();
        return ElasticsearchUtil.scrollAll(client, searchRequest, IncidentPo.class);
    }

}
