package com.brandnewdata.mop.poc.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.sniff.SniffOnFailureListener;
import org.elasticsearch.client.sniff.Sniffer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfiguration {

    @Bean(destroyMethod = "close")
    public ElasticsearchTransport restClientTransport() {
        SniffOnFailureListener sniffOnFailureListener =
                new SniffOnFailureListener();
        RestClient restClient = RestClient.builder(
                        new HttpHost("localhost", 49200))
                .setFailureListener(sniffOnFailureListener)
                .build();
        Sniffer sniffer = Sniffer.builder(restClient)
                .setSniffAfterFailureDelayMillis(30000)
                .build();
        sniffOnFailureListener.setSniffer(sniffer);
        return new RestClientTransport(restClient, new JacksonJsonpMapper());
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }

}
