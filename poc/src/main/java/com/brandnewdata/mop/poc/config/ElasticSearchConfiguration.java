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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

@Configuration
public class ElasticSearchConfiguration {
    @Bean
    public ElasticsearchClient restClientTransport(
            @Value("${brandnewdata.elasticsearch.uris}") String[] uris) {

        // 支持jdk8
        ObjectMapper objectMapper = JsonMapper.builder() // or different mapper for other format
                // .addModule(new JavaTimeModule())
                // and possibly other configuration, modules, then:
                .build();

        // 自动发现注册引入的模块
        objectMapper.findAndRegisterModules();

        HttpHost[] httpHosts = Arrays.stream(uris).map(this::createHttpHost).toArray(HttpHost[]::new);

        // Create the low-level client
        RestClient restClient = RestClient.builder(httpHosts).build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper(objectMapper));

        // And create the API client

        return new ElasticsearchClient(transport);
    }

    private HttpHost createHttpHost(String uri) {
        try {
            return createHttpHost(URI.create(uri));
        }
        catch (IllegalArgumentException ex) {
            return HttpHost.create(uri);
        }
    }

    private HttpHost createHttpHost(URI uri) {
        if (!StringUtils.hasLength(uri.getUserInfo())) {
            return HttpHost.create(uri.toString());
        }
        try {
            return HttpHost.create(new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(),
                    uri.getQuery(), uri.getFragment()).toString());
        }
        catch (URISyntaxException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
