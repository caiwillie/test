package com.brandnewdata.mop.poc.bff.converter.proxy;

import cn.hutool.core.lang.Opt;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.brandnewdata.mop.poc.bff.vo.proxy.ProxyVo;
import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;

public class ProxyDtoConverter {

    public static ProxyDto createFrom(ProxyVo proxyVo) {
        ProxyDto proxyDto = new ProxyDto();
        proxyDto.setId(proxyVo.getId());
        proxyDto.setName(proxyVo.getName());
        proxyDto.setVersion(proxyVo.getVersion());
        proxyDto.setProtocol(proxyVo.getProtocol());
        proxyDto.setDescription(proxyVo.getDescription());
        // null 转换为 ""
        proxyDto.setTag(Opt.ofNullable(proxyVo.getTag()).orElse(StringPool.EMPTY));
        proxyDto.setState(proxyVo.getState());
        proxyDto.setProjectId(Opt.ofNullable(proxyVo.getProjectId()).map(Long::valueOf).orElse(null));
        return proxyDto;
    }
}
