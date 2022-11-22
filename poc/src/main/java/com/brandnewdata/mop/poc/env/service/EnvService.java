package com.brandnewdata.mop.poc.env.service;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import com.brandnewdata.mop.poc.env.cache.EnvCache;
import com.brandnewdata.mop.poc.env.cache.EnvServiceCache;
import com.brandnewdata.mop.poc.env.dto.EnvDto;
import com.brandnewdata.mop.poc.env.dto.EnvServiceDto;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnvService implements IEnvService {

    @Resource
    private EnvCache envCache;

    @Resource
    private EnvServiceCache envServiceCache;

    @Override
    public List<EnvDto> listEnv() {
        return ListUtil.toList(envCache.asMap().values());
    }

    @Override
    public List<EnvServiceDto> listEnvService(Long envId) {
        Assert.notNull(envId, "环境不能为空");
        return envServiceCache.asMap().values().stream()
                .filter(envServiceDto -> NumberUtil.equals(envServiceDto.getEnvId(), envServiceDto.getEnvId()))
                .collect(Collectors.toList());
    }
}
