package com.brandnewdata.mop.poc.es;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.thread.ThreadUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.ExistsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.brandnewdata.mop.poc.operate.schema.template.ListViewTemplate;
import com.brandnewdata.mop.poc.util.HttpHostUtil;
import com.caiwillie.util.cache.ElasticsearchUtil;
import com.caiwillie.util.cache.ScheduleScanEsCache;
import com.dxy.library.json.jackson.JacksonUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.cache.Cache;
import lombok.SneakyThrows;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class EsTest {

    private static ElasticsearchClient client;
    @BeforeAll
    static void init() {
        HttpHost httpHost = HttpHostUtil.createHttpHost("es-connector1.basic.dev.brandnewdata.com:8080");

        // Create the low-level client
        RestClient restClient = RestClient.builder(new HttpHost[]{httpHost}).build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper(JacksonUtil.getObjectMapper()));

        // And create the API client
        client = new ElasticsearchClient(transport);
    }

    @SneakyThrows
    @Test
    void health() {
        HealthResponse health = client.cluster().health();
        System.out.println(health);
    }

    @SneakyThrows
    @Test
    void test1() {

        OffsetDateTime date = OffsetDateTime.now();
        List<Query> mustList = new ArrayList<>();
        mustList.add(new Query.Builder().range(new RangeQuery.Builder().field("startDate")
                .format(DatePattern.UTC_SIMPLE_MS_PATTERN)
                .timeZone("+08:00")
                .gt(JsonData.of("2000-01-01T00:00:00.000")).build()).build());
        mustList.add(new Query.Builder().exists(new ExistsQuery.Builder().field("id").build()).build());

        SearchRequest request = new SearchRequest.Builder()
                .index("operate-list-view-1.3.0_alias")
                .query(new Query.Builder().bool(new BoolQuery.Builder().must(mustList).build()).build())
                .build();
        List<ObjectNode> entities = ElasticsearchUtil.scrollAll(client, request, ObjectNode.class);

        return;
    }

    @Test
    void test2() {

        BoolQuery filter = new BoolQuery.Builder()
                .must(new Query.Builder().term(t -> t.field("joinRelation").value("processInstance")).build())
                .build();


        new ScheduleScanEsCache("operate-list-view-1.3.0_alias", "id",
                "startDate", client, "0/4 * * * * ?", filter,
                (BiConsumer<List<ObjectNode>, Cache>) (objectNodes, cache) -> {
                    return;
                });
        ThreadUtil.sleep(1000000);
    }

    @Test
    void test3() {
        String format = DateUtil.format(DateUtil.date(), DatePattern.UTC_MS_WITH_XXX_OFFSET_PATTERN);
        DateTime parse = DateUtil.parse("2022-12-14T13:25:55.507+08:00", DatePattern.UTC_MS_WITH_XXX_OFFSET_PATTERN);
        return;
    }

}
