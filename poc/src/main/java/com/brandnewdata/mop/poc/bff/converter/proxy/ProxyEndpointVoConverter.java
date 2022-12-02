package com.brandnewdata.mop.poc.bff.converter.proxy;

import com.brandnewdata.mop.poc.bff.vo.proxy.ProxyEndpointVo;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto;

public class ProxyEndpointVoConverter {

    public static ProxyEndpointVo createFrom(ProxyEndpointDto dto) {
        ProxyEndpointVo vo = new ProxyEndpointVo();
        vo.setId(dto.getId());
        vo.setProxyId(dto.getProxyId());
        vo.setLocation(dto.getLocation());
        vo.setBackendType(dto.getBackendType());
        vo.setBackendConfig(dto.getBackendConfig());
        vo.setDescription(dto.getDescription());
        vo.setTag(dto.getTag());
        return vo;
    }
}
