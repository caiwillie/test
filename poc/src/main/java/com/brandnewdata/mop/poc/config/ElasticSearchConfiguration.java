package com.brandnewdata.mop.poc.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.springframework.beans.factory.annotation.Value;

public class ElasticSearchConfiguration {

    public ElasticsearchClient restClientTransport(
            @Value("${brandnewdata.elasticsearch.uris}") String[] uris) {

       /* // 支持jdk8
        ObjectMapper objectMapper = JsonMapper.builder() // or different mapper for other format
                // .addModule(new JavaTimeModule())
                // and possibly other configuration, modules, then:
                .build();

        // 自动发现注册引入的模块
        objectMapper.findAndRegisterModules();

        // 设置 unknow properties 时不报错
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        HttpHost[] httpHosts = Arrays.stream(uris).map(this::createHttpHost).toArray(HttpHost[]::new);

        // Create the low-level client
        RestClient restClient = RestClient.builder(httpHosts).build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper(objectMapper));

        // And create the API client

        return new ElasticsearchClient(transport);*/
        return null;
    }



}
