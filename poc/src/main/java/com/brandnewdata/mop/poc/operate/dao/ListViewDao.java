package com.brandnewdata.mop.poc.operate.dao;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import com.brandnewdata.mop.poc.operate.po.listview.ProcessInstanceForListViewPo;
import com.brandnewdata.mop.poc.operate.schema.template.ListViewTemplate;
import com.brandnewdata.mop.poc.operate.util.ElasticsearchUtil;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.HashMap;
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
    public List<ProcessInstanceForListViewPo> searchList(Query query, Integer from, Integer size,
                                                         List<SortOptions> sortOptions, ElasticsearchUtil.QueryType queryType) {
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index(ElasticsearchUtil.whereToSearch(TEMPLATE, queryType))
                .query(query)
                .from(from)
                .size(size);

        if(CollUtil.isNotEmpty(sortOptions)) builder.sort(sortOptions);

        SearchRequest request = builder.build();

        return ElasticsearchUtil.searchList(client, request, ProcessInstanceForListViewPo.class);
    }

    @SneakyThrows
    public List<CompositeBucket> aggregation(Query query,
                                             List<Map<String, CompositeAggregationSource>> sourceList,
                                             ElasticsearchUtil.QueryType queryType) {
        List<CompositeBucket> buckets = new ArrayList<>();

        List<CompositeBucket> tempBuckets = null;
        Map<String, String> afterMap = new HashMap<>();

        do {
            CompositeAggregation.Builder compositeAggregationBuilder = new CompositeAggregation.Builder();
            compositeAggregationBuilder.sources(sourceList).size(400);
            if (CollUtil.isNotEmpty(afterMap)) {
                compositeAggregationBuilder.after(afterMap);
            }

            Aggregation aggregation = new Aggregation.Builder()
                    .composite(compositeAggregationBuilder.build())
                    .build();

            SearchRequest request = new SearchRequest.Builder()
                    .index(ElasticsearchUtil.whereToSearch(TEMPLATE, queryType))
                    .query(query)
                    .aggregations("my_agg", aggregation)
                    .size(0)
                    .build();
            SearchResponse<ProcessInstanceForListViewPo> response = client.search(request, ProcessInstanceForListViewPo.class);

            CompositeAggregate myAgg = (CompositeAggregate) response.aggregations().get("my_agg")._get();

            tempBuckets = myAgg.buckets().array();

            if (CollUtil.isNotEmpty(tempBuckets) && CollUtil.isNotEmpty(afterMap)) {
                tempBuckets.remove(0);
            }

            Map<String, JsonData> afterKeyMap = myAgg.afterKey();
            if (CollUtil.isNotEmpty(afterKeyMap)) {
                afterKeyMap.forEach((s, jsonData) -> {
                    afterMap.put(s, jsonData.toString());
                });
            }

            if (CollUtil.isNotEmpty(tempBuckets)) {
                buckets.addAll(tempBuckets);
            }
        } while (CollUtil.isNotEmpty(tempBuckets));

        return buckets;
    }

}
