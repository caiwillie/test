package com.brandnewdata.mop.poc.operate.manager;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.brandnewdata.mop.poc.env.dto.EnvDto;
import com.brandnewdata.mop.poc.env.dto.EnvServiceDto;
import com.brandnewdata.mop.poc.env.service.IEnvService;
import com.brandnewdata.mop.poc.util.HttpHostUtil;
import com.dxy.library.json.jackson.JacksonUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ElasticsearchManager {

    private final IEnvService envService;

    private final ObjectMapper objectMapper;

    private final LoadingCache<Long, ElasticsearchClient> cache;

    public ElasticsearchManager(IEnvService envService,
                                @Value("${brandnewdata.cloud-native.elasticsearch.http-port}") Integer port) {
        this.envService = envService;
        this.objectMapper = initJackson();
        this.cache = CacheBuilder.newBuilder().build(getCacheLoader());
    }

    @SneakyThrows
    public ElasticsearchClient getByEnvId(Long envId) {
        return cache.get(envId);
    }

    private ObjectMapper initJackson() {
        ObjectMapper objectMapper = JsonMapper.builder().build();
        // 自动发现注册引入的模块
        objectMapper.findAndRegisterModules();
        // 设置 unknow properties 时不报错
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    private CacheLoader<Long, ElasticsearchClient> getCacheLoader() {
        return new CacheLoader<Long, ElasticsearchClient>() {
            @Override
            public ElasticsearchClient load(Long key) throws Exception {
                EnvDto envDto = envService.fetchOne(key);
                Assert.notNull(envDto, "环境不存在");
                Optional<EnvServiceDto> serviceOpt = envService.fetchEnvService(key).stream()
                        .filter(envServiceDto -> StrUtil.equals(envServiceDto.getName(), "elasticsearch-master"))
                        .findFirst();

                Assert.isTrue(serviceOpt.isPresent(), "环境信息配置有误");

                EnvServiceDto envServiceDto = serviceOpt.get();

                HttpHost httpHost = HttpHostUtil.createHttpHost(StrUtil.format("{}.{}:{}",
                        envServiceDto.getName(), envDto.getNamespace(), envServiceDto.getPorts()));

                // Create the low-level client
                RestClient restClient = RestClient.builder(new HttpHost[]{httpHost}).build();

                // Create the transport with a Jackson mapper
                ElasticsearchTransport transport = new RestClientTransport(
                        restClient, new JacksonJsonpMapper(JacksonUtil.getObjectMapper()));

                // And create the API client
                return new ElasticsearchClient(transport);
            }
        };
    }
}
