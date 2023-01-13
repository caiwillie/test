package com.brandnewdata.mop.poc.bff.converter.proxy;

import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.bff.vo.proxy.ProxyVo;
import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;

public class ProxyDtoConverter {

    public static ProxyDto createFrom(ProxyVo proxyVo) {
        ProxyDto proxyDto = new ProxyDto();
        proxyDto.setId(proxyVo.getId());
        proxyDto.setName(StrUtil.trim(proxyVo.getName()));
        proxyDto.setVersion(proxyVo.getVersion());
        proxyDto.setProtocol(proxyVo.getProtocol());
        proxyDto.setDescription(proxyVo.getDescription());
        proxyDto.setTag(StrUtil.trim(proxyVo.getTag()));
        proxyDto.setState(proxyVo.getState());
        proxyDto.setProjectId(Opt.ofNullable(proxyVo.getProjectId()).map(Long::valueOf).orElse(null));
        return proxyDto;
    }
}
