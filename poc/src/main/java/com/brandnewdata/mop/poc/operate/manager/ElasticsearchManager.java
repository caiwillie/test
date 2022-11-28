package com.brandnewdata.mop.poc.operate.manager;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.brandnewdata.mop.poc.env.service.IEnvService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ElasticsearchManager {

    private final IEnvService envService;

    @Value("${brandnewdata.cloud-native.elasticsearch.http-port}")
    private Integer prot;

    public ElasticsearchManager(IEnvService envService) {
        this.envService = envService;
    }

    public ElasticsearchClient getByEnvId(Long envId) {
        return null;
    }
}
