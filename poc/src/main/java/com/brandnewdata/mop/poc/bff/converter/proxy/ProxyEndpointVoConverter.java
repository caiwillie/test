package com.brandnewdata.mop.poc.bff.converter.proxy;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.brandnewdata.mop.poc.bff.vo.proxy.ProxyEndpointVo;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto;

public class ProxyEndpointVoConverter {

    public static ProxyEndpointVo createFrom(ProxyEndpointDto dto) {
        ProxyEndpointVo vo = new ProxyEndpointVo();
        vo.setId(String.valueOf(dto.getId()));
        vo.setProxyId(dto.getProxyId());
        vo.setLocation(dto.getLocation());
        vo.setBackendType(dto.getBackendType());
        vo.setBackendConfig(dto.getBackendConfig());
        vo.setDescription(dto.getDescription());
        vo.setTag(StringPool.EMPTY.equals(dto.getTag()) ? null : dto.getTag());
        vo.setUpdateTime(dto.getUpdateTime());
        return vo;
    }
}
