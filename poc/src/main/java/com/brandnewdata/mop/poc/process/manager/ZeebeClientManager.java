package com.brandnewdata.mop.poc.process.manager;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
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

import java.util.Optional;

@Component
public class ZeebeClientManager {

    private final IEnvService envService;

    private final LoadingCache<Long, ZeebeClient> cache;

    public ZeebeClientManager(IEnvService envService,
                              @Value("${brandnewdata.cloud-native.zeebe.zeebe-gateway.grpc-port}") Integer grpcPort) {
        this.envService = envService;
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
                String serviceName = envServiceDto.getName();
                String ports = envServiceDto.getPorts();
                String namespace = envDto.getNamespace();
                ZeebeClient client = ZeebeClient.newClientBuilder()
                        .gatewayAddress(StrUtil.format("{}.{}:{}", serviceName, namespace, ports))
                        .usePlaintext()
                        .build();
                return client;
            }
        };
    }

}
