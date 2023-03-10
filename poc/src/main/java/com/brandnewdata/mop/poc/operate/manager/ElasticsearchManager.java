package com.brandnewdata.mop.poc.operate.manager;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.brandnewdata.mop.poc.env.config.CloudNativeConfigure;
import com.brandnewdata.mop.poc.env.dto.EnvDto;
import com.brandnewdata.mop.poc.env.dto.EnvServiceDto;
import com.brandnewdata.mop.poc.env.service.IEnvService;
import com.brandnewdata.mop.poc.util.HttpHostUtil;
import com.dxy.library.json.jackson.JacksonUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class ElasticsearchManager {

    private final IEnvService envService;

    private final Map<String, Integer> debugServicePortMap;

    private final LoadingCache<Long, ElasticsearchClient> cache;


    public ElasticsearchManager(IEnvService envService,
                                CloudNativeConfigure cloudNativeConfigure) {
        this.envService = envService;
        this.debugServicePortMap = Opt.ofNullable(cloudNativeConfigure)
                .map(CloudNativeConfigure::getDebugServicePort).orElse(MapUtil.empty());
        this.cache = CacheBuilder.newBuilder().build(getCacheLoader());
    }

    @SneakyThrows
    public ElasticsearchClient getByEnvId(Long envId) {
        return cache.get(envId);
    }

    private CacheLoader<Long, ElasticsearchClient> getCacheLoader() {
        return new CacheLoader<Long, ElasticsearchClient>() {
            @Override
            public ElasticsearchClient load(Long key) throws Exception {
                EnvDto envDto = envService.fetchOne(key);
                Assert.notNull(envDto, "???????????????");
                Optional<EnvServiceDto> serviceOpt = envService.fetchEnvService(key).stream()
                        .filter(envServiceDto -> StrUtil.equals(envServiceDto.getName(), "elasticsearch-master"))
                        .findFirst();
                if(!serviceOpt.isPresent()) {
                    throw new RuntimeException("????????????????????????");
                }
                EnvServiceDto envServiceDto = serviceOpt.get();

                // service domain port
                String serviceDomain = StrUtil.format("{}.{}", envServiceDto.getName(), envDto.getNamespace());
                Integer port = Opt.ofNullable(debugServicePortMap.get(serviceDomain))
                        .orElseGet(() -> Integer.parseInt(envServiceDto.getPorts()));

                HttpHost httpHost = HttpHostUtil.createHttpHost(StrUtil.format("{}:{}", serviceDomain, port));

                // Create the low-level client
                RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost[]{httpHost});
                restClientBuilder.setHttpClientConfigCallback(
                        requestConfig -> requestConfig.setKeepAliveStrategy((response, context) -> TimeUnit.MINUTES.toMinutes(5)));

                restClientBuilder.setRequestConfigCallback(requestConfigBuilder ->
                        requestConfigBuilder.setConnectTimeout(30000).setSocketTimeout(300 * 1000));


                // Create the transport with a Jackson mapper
                ElasticsearchTransport transport = new RestClientTransport(
                        restClientBuilder.build(), new JacksonJsonpMapper(JacksonUtil.getObjectMapper()));

                // And create the API client
                return new ElasticsearchClient(transport);
            }
        };
    }
}
