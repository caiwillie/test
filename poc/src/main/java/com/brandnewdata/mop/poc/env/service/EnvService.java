package com.brandnewdata.mop.poc.env.service;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.NumberUtil;
import com.brandnewdata.mop.poc.constant.EnvConst;
import com.brandnewdata.mop.poc.env.cache.EnvCache;
import com.brandnewdata.mop.poc.env.cache.EnvServiceCache;
import com.brandnewdata.mop.poc.env.dto.EnvDto;
import com.brandnewdata.mop.poc.env.dto.EnvServiceDto;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EnvService implements IEnvService {

    @Resource
    private EnvCache envCache;

    @Resource
    private EnvServiceCache envServiceCache;

    @Override
    public EnvDto fetchOne(Long envId) {
        return envCache.asMap().get(envId);
    }

    @Override
    public List<EnvDto> fetchEnvList() {
        return ListUtil.toList(envCache.asMap().values());
    }

    @Override
    public List<EnvServiceDto> fetchEnvService(Long envId) {
        Assert.notNull(envId, "环境不能为空");
        return envServiceCache.asMap().values().stream()
                .filter(envServiceDto -> NumberUtil.equals(envId, envServiceDto.getEnvId()))
                .collect(Collectors.toList());
    }

    @Override
    public EnvDto fetchDebugEnv() {
        // 获取调试环境
        Optional<EnvDto> debugEnvOpt = fetchEnvList().stream()
                .filter(envDto -> NumberUtil.equals(envDto.getType(), EnvConst.ENV_TYPE__SANDBOX)).findFirst();
        if(debugEnvOpt.isPresent()) {
            return debugEnvOpt.get();
        } else {
            throw new RuntimeException("调试环境不存在");
        }
    }
}
