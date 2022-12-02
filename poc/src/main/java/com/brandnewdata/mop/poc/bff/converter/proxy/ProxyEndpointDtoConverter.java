package com.brandnewdata.mop.poc.bff.converter.proxy;

import com.brandnewdata.mop.poc.bff.vo.proxy.ProxyEndpointVo;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto;

public class ProxyEndpointDtoConverter {

    public static ProxyEndpointDto createFrom(ProxyEndpointVo vo) {
        ProxyEndpointDto dto = new ProxyEndpointDto();
        dto.setId(vo.getId());
        dto.setProxyId(vo.getProxyId());
        dto.setLocation(vo.getLocation());
        dto.setBackendType(vo.getBackendType());
        dto.setBackendConfig(vo.getBackendConfig());
        dto.setDescription(vo.getDescription());
        dto.setTag(vo.getTag());
        return dto;
    }
}
