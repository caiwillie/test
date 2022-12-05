package com.brandnewdata.mop.poc.bff.converter.proxy;

import com.brandnewdata.mop.poc.bff.vo.proxy.operate.ProxyEndpointCallVo;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointCallDto;

public class ProxyEndpointCallVoConverter {

    public static ProxyEndpointCallVo createFrom(ProxyEndpointCallDto dto) {
        ProxyEndpointCallVo vo = new ProxyEndpointCallVo();
        vo.setId(String.valueOf(dto.getId()));
        vo.setEndpointId(dto.getEndpointId());
        vo.setProxyId(dto.getProxyId());
        vo.setProxyName(dto.getProxyName());
        vo.setLocation(dto.getLocation());
        vo.setHttpMethod(dto.getHttpMethod());
        vo.setExecuteStatus(dto.getExecuteStatus());
        vo.setRequestBody(dto.getRequestBody());
        vo.setErrorMessage(dto.getErrorMessage());
        return vo;
    }

}
