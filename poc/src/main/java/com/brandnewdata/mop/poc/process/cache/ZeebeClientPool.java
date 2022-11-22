package com.brandnewdata.mop.poc.process.cache;

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
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Optional;

@Component
public class ZeebeClientPool {

    @Resource
    private IEnvService envService;

    private final LoadingCache<Long, ZeebeClient> CACHE = CacheBuilder.newBuilder().build(getCacheLoader());

    @SneakyThrows
    public ZeebeClient getByEnv(Long envId) {
        return CACHE.get(envId);
    }

    private CacheLoader<Long, ZeebeClient> getCacheLoader() {
        return new CacheLoader<Long, ZeebeClient>() {
            @Override
            public ZeebeClient load(Long key) throws Exception {
                EnvDto envDto = envService.getOne(key);
                Assert.notNull(envDto, "环境不存在");
                Optional<EnvServiceDto> serviceOpt = envService.listEnvService(key).stream().filter(
                                envServiceDto -> StrUtil.equals(envServiceDto.getName(), "camunda-platform-zeebe-gateway"))
                        .findFirst();
                Assert.isTrue(serviceOpt.isPresent(), "【ENV01】获取环境信息有误");
                EnvServiceDto envServiceDto = serviceOpt.get();
                String clusterIp = envServiceDto.getClusterIp();
                ZeebeClient client = ZeebeClient.newClientBuilder()
                        .gatewayAddress(StrUtil.format("{}:26500", clusterIp))
                        .usePlaintext()
                        .build();
                return client;
            }
        };
    }

    public static void main(String[] args) {

    }

}
