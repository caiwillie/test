package com.brandnewdata.mop.poc.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.brandnewdata.mop.poc.operate.schema.template.ListViewTemplate;
import com.brandnewdata.mop.poc.util.HttpHostUtil;
import com.caiwillie.util.cache.ScheduleScanEsCache;
import com.dxy.library.json.jackson.JacksonUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.cache.Cache;
import lombok.SneakyThrows;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
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

    @Test
    void test() {
        BoolQuery filter = new BoolQuery.Builder()
                .must(new Query.Builder().term(t -> t.field("joinRelation").value("processInstance")).build())
                .build();

        new ScheduleScanEsCache("operate-list-view-1.3.0_alias", "_id",
                "startDate", client, "0/4 * * * * ?", filter,
                (BiConsumer<List<ObjectNode>, Cache>) (objectNodes, cache) -> {
                    return;
                });
    }


}
