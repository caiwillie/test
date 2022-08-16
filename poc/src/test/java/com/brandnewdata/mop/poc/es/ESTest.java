package com.brandnewdata.mop.poc.es;

import cn.hutool.core.collection.ListUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.brandnewdata.mop.poc.operate.util.ElasticsearchUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class ESTest {

    @Autowired
    private ElasticsearchClient client;

    @SneakyThrows
    @Test
    void test() {
        GetResponse<ObjectNode> response = client.get(g -> g
                        .index("operate-flownode-instance-1.3.0_2022-08-03")
                        .id("2251799813685256"),
                ObjectNode.class
        );

        return;
    }

    @SneakyThrows
    @Test
    void test2() {
        SearchResponse<ObjectNode> response = client.search(s -> s
                        .index("operate-flownode-instance-1.3.0_alias")
                        .query(q -> q.term(t -> t
                                        .field("processInstanceKey").value("2251799813685280")
                                )
                        ),
                ObjectNode.class
        );

        return;
    }


    @Test
    void scrollAll() {

        Time time = new Time.Builder().time("1m").build();

        SearchRequest searchRequest = new SearchRequest.Builder().index("operate-flownode-instance-1.3.0_alias").build();

        List<ObjectNode> documents = ElasticsearchUtil.scrollAll(client,
                searchRequest,
                ObjectNode.class);

        return;
    }
}
