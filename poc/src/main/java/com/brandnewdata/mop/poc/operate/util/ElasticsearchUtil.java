package com.brandnewdata.mop.poc.operate.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.PointInTimeReference;
import com.aliyuncs.kms.transform.v20160120.ListAliasesResponseUnmarshaller;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ElasticsearchUtil {


    @SneakyThrows
    public static <TDocument> List<TDocument> scrollAll(ElasticsearchClient client,
                                                        SearchRequest searchRequest,
                                                        Class<TDocument> tDocumentClass) {
        Time time = Time.of(b -> b.time("1m"));

        // 获取初始 pit
        String pit = openPointInTime(client, searchRequest.index(), time);

        // 获取老数据的 query 部分
        Query query = searchRequest.query();

        // 获取原先的 sort list，并新增一个隐含排序
        List<SortOptions> sortList = ListUtil.toList(searchRequest.sort());
        sortList.add(SortOptions.of(b -> b.field(f -> f.field("_shard_doc").order(SortOrder.Desc))));

        List<TDocument> ret = new ArrayList<>();
        List<TDocument> sourceList = null;
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
            if(CollUtil.isNotEmpty(searchAfter)) builder.searchAfter(searchAfter);

            SearchRequest request = builder.build();
            SearchResponse<TDocument> response = client.search(request, tDocumentClass);
            List<Hit<TDocument>> hits = response.hits().hits();

            sourceList = hits.stream().map(Hit::source).collect(Collectors.toList());

            // 设置下一次的builder
            if(CollUtil.isNotEmpty(hits)) {
                ret.addAll(sourceList);
                Hit<TDocument> lastHit = hits.get(hits.size() - 1);

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
