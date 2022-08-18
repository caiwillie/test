package com.brandnewdata.mop.poc.operate.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.PointInTimeReference;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ElasticsearchUtil {

    @SneakyThrows
    public static <T> List<T> scrollAll(ElasticsearchClient client,
                                        SearchRequest searchRequest,
                                        Class<T> clazz) {
        Time time = Time.of(b -> b.time("1m"));

        // 获取初始 pit
        String pit = openPointInTime(client, searchRequest.index(), time);

        // 获取老数据的 query 部分
        Query query = searchRequest.query();

        // 获取原先的 sort list，并新增一个隐含排序
        List<SortOptions> sortList = ListUtil.toList(searchRequest.sort());
        sortList.add(SortOptions.of(b -> b.field(f -> f.field("_shard_doc").order(SortOrder.Desc))));

        List<T> ret = new ArrayList<>();
        List<T> sourceList = null;
        List<String> searchAfter = new ArrayList<>();

        do {

            // 构造 pit
            PointInTimeReference pointInTimeReference = new PointInTimeReference.Builder()
                    .id(pit)
                    .keepAlive(time)
                    .build();

            SearchRequest.Builder builder = new SearchRequest.Builder();

            builder.pit(pointInTimeReference)
                    .query(query)
                    .sort(sortList);
            // 如果 search after 不为空，就直接构造
            if(CollUtil.isNotEmpty(searchAfter)) builder.searchAfter(searchAfter);

            SearchRequest newRequest = builder.build();
            SearchResponse<T> response = client.search(newRequest, clazz);
            List<Hit<T>> hits = response.hits().hits();

            sourceList = hits.stream().map(mapSearchHits()).collect(Collectors.toList());

            // 设置下一次的builder
            if(CollUtil.isNotEmpty(hits)) {
                ret.addAll(sourceList);
                Hit<T> lastHit = hits.get(hits.size() - 1);

                // 最后一个hit的sort作为search after
                searchAfter = lastHit.sort();

                // 更新 pit
                pit = response.pitId();
            }

        } while (CollUtil.isNotEmpty(sourceList));

        closePointInTime(client, pit);// 最后一次查询结果为空就退出

        return ret;
    }

    @SneakyThrows
    public static <T> T searchOne(ElasticsearchClient client,
                                  SearchRequest searchRequest,
                                  Class<T> clazz) {
        SearchResponse<T> response = client.search(searchRequest, clazz);
        List<Hit<T>> hits = response.hits().hits();

        if(CollUtil.isEmpty(hits)) {
            // 为空返回 null
            return null;
        } else {
            return hits.get(0).source();
        }
    }

    @SneakyThrows
    public static <T> T getOne(ElasticsearchClient client, GetRequest getRequest, Class<T> clazz) {
        GetResponse<T> response = client.get(getRequest, clazz);
        if(response == null) {
            return null;
        } else {
            return response.source();
        }
    }

    private static <TDocument> Function<Hit<TDocument>, TDocument> mapSearchHits() {
        return Hit::source;
    }


    @SneakyThrows
    public static String openPointInTime(ElasticsearchClient client, List<String> indices, Time keepAlive) {
        SearchRequest searchRequest;

        OpenPointInTimeRequest request = new OpenPointInTimeRequest.Builder()
                .index(indices)
                .keepAlive(keepAlive)
                .build();

        OpenPointInTimeResponse response = client.openPointInTime(request);
        return response.id();
    }

    @SneakyThrows
    public static void closePointInTime(ElasticsearchClient client, String pointInTime) {
        ClosePointInTimeRequest request = new ClosePointInTimeRequest.Builder()
                .id(pointInTime)
                .build();
        ClosePointInTimeResponse response = client.closePointInTime(request);
    }

}
