package com.brandnewdata.mop.poc.process.manager;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.env.config.CloudNativeConfigure;
import com.brandnewdata.mop.poc.env.dto.EnvDto;
import com.brandnewdata.mop.poc.env.dto.EnvServiceDto;
import com.brandnewdata.mop.poc.env.service.IEnvService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.camunda.zeebe.client.ZeebeClient;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class ZeebeClientManager {

    private final IEnvService envService;

    private final Map<String, Integer> debugServicePortMap;

    private final LoadingCache<Long, ZeebeClient> cache;

    public ZeebeClientManager(IEnvService envService,
                              CloudNativeConfigure cloudNativeConfigure) {
        this.envService = envService;
        this.debugServicePortMap = Opt.ofNullable(cloudNativeConfigure)
                .map(CloudNativeConfigure::getDebugServicePort).orElse(MapUtil.empty());
        this.cache = CacheBuilder.newBuilder().build(getCacheLoader());
    }

    @SneakyThrows
    public ZeebeClient getByEnvId(Long envId) {
        return cache.get(envId);
    }

    private CacheLoader<Long, ZeebeClient> getCacheLoader() {
        return new CacheLoader<Long, ZeebeClient>() {
            @Override
            public ZeebeClient load(Long key) throws Exception {
                EnvDto envDto = envService.fetchOne(key);
                Assert.notNull(envDto, "环境不存在");
                Optional<EnvServiceDto> serviceOpt = envService.fetchEnvService(key).stream()
                        .filter(envServiceDto -> StrUtil.equals(envServiceDto.getName(), "camunda-platform-zeebe-gateway"))
                        .findFirst();
                Assert.isTrue(serviceOpt.isPresent(), "环境信息配置有误");
                EnvServiceDto envServiceDto = serviceOpt.get();

                // service domain port
                String serviceDomain = StrUtil.format("{}.{}", envServiceDto.getName(), envDto.getNamespace());
                Integer port = Opt.ofNullable(debugServicePortMap.get(serviceDomain))
                        .orElseGet(() -> Integer.parseInt(envServiceDto.getPorts()));

                ZeebeClient client = ZeebeClient.newClientBuilder()
                        .gatewayAddress(StrUtil.format("{}:{}", serviceDomain, port))
                        .usePlaintext()
                        .build();
                return client;
            }
        };
    }

}
