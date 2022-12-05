package com.brandnewdata.mop.poc.bff.converter.proxy;

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
        proxyDto.setTag(proxyVo.getTag());
        proxyDto.setState(proxyVo.getState());
        return proxyDto;
    }
}
