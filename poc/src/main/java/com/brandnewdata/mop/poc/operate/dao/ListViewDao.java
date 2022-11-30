package com.brandnewdata.mop.poc.operate.dao;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.brandnewdata.mop.poc.operate.po.listview.ProcessInstanceForListViewPo;
import com.brandnewdata.mop.poc.operate.schema.template.ListViewTemplate;
import com.brandnewdata.mop.poc.operate.util.ElasticsearchUtil;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Map;

public class ListViewDao {
    private static final ListViewTemplate TEMPLATE = new ListViewTemplate();

    private static final Map<ElasticsearchClient, ListViewDao> INSTANCE_MAP = MapUtil.newConcurrentHashMap();

    private final ElasticsearchClient client;

    private ListViewDao(ElasticsearchClient client) {
        this.client = client;
    }

    public static ListViewDao getInstance(ElasticsearchClient client) {
        return INSTANCE_MAP.computeIfAbsent(client, ListViewDao::new);
    }

    public ProcessInstanceForListViewPo getOneByParentFlowNodeInstanceId(String parentFlowNodeInstanceId) {
        Assert.notNull(parentFlowNodeInstanceId);
        SearchRequest request = new SearchRequest.Builder()
                .index(TEMPLATE.getAlias())
                .query(new Query.Builder()
                        .term(new TermQuery.Builder()
                                .field(ListViewTemplate.PARENT_FLOW_NODE_INSTANCE_KEY)
                                .value(parentFlowNodeInstanceId)
                                .build())
                        .build())
                .build();
        return ElasticsearchUtil.searchOne(client, request, ProcessInstanceForListViewPo.class);
    }

    public ProcessInstanceForListViewPo searchOne(Query query) {
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(TEMPLATE.getAlias())
                .query(query)
                .build();
        return ElasticsearchUtil.searchOne(client, searchRequest, ProcessInstanceForListViewPo.class);
    }

    public List<ProcessInstanceForListViewPo> scrollAll(Query query, ElasticsearchUtil.QueryType queryType) {
        SearchRequest request = new SearchRequest.Builder()
                .index(ElasticsearchUtil.whereToSearch(TEMPLATE, queryType))
                .query(query)
                .build();
        return ElasticsearchUtil.scrollAll(client, request, ProcessInstanceForListViewPo.class);
    }

    @SneakyThrows
    public SearchResponse<ProcessInstanceForListViewPo> search(Query query, Map<String, Aggregation> aggs, ElasticsearchUtil.QueryType queryType) {
        SearchRequest request = new SearchRequest.Builder()
                .index(ElasticsearchUtil.whereToSearch(TEMPLATE, queryType))
                .query(query)
                .aggregations(aggs)
                .build();

        SearchResponse<ProcessInstanceForListViewPo> response = client.search(request, ProcessInstanceForListViewPo.class);
        return response;
    }

}
