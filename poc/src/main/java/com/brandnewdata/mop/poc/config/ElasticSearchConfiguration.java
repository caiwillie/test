package com.brandnewdata.mop.poc.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.sniff.SniffOnFailureListener;
import org.elasticsearch.client.sniff.Sniffer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfiguration {

    @Bean
    public ElasticsearchClient restClientTransport() {

        // 支持jdk8
        ObjectMapper objectMapper = JsonMapper.builder() // or different mapper for other format
                // .addModule(new JavaTimeModule())
                // and possibly other configuration, modules, then:
                .build();

        objectMapper.findAndRegisterModules();

        // Create the low-level client
        RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 49200)).build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper(objectMapper));

        // And create the API client
        ElasticsearchClient client = new ElasticsearchClient(transport);

        return client;
    }

}
