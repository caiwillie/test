package com.brandnewdata.mop.poc.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ESTest {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @SneakyThrows
    @Test
    void test() {
        GetResponse<ObjectNode> response = elasticsearchClient.get(g -> g
                        .index("operate-flownode-instance-1.3.0_2022-08-03")
                        .id("2251799813685256"),
                ObjectNode.class
        );

        while(true) {
            Thread.sleep(2000);
        }

    }
}
