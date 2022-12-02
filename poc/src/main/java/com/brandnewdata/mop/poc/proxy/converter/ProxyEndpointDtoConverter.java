package com.brandnewdata.mop.poc.proxy.converter;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Opt;
import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto;
import com.brandnewdata.mop.poc.proxy.po.ProxyEndpointPo;

public class ProxyEndpointDtoConverter {

    // copy properties from dto to po
    public static ProxyEndpointDto createFrom(ProxyEndpointPo po) {
        ProxyEndpointDto dto = new ProxyEndpointDto();
        dto.setId(po.getId());
        dto.setCreateTime(Opt.ofNullable(po.getCreateTime()).map(LocalDateTimeUtil::of).orElse(null));
        dto.setUpdateTime(Opt.ofNullable(po.getUpdateTime()).map(LocalDateTimeUtil::of).orElse(null));
        dto.setProxyId(po.getProxyId());
        dto.setLocation(po.getLocation());
        dto.setDescription(po.getDescription());
        dto.setBackendType(po.getBackendType());
        dto.setBackendConfig(po.getBackendConfig());
        dto.setTag(po.getTag());
        return dto;
    }

    public static void updateFrom(ProxyEndpointDto target, ProxyEndpointDto dto) {
        target.setLocation(dto.getLocation());
        target.setDescription(dto.getDescription());
        target.setBackendType(dto.getBackendType());
        target.setBackendConfig(dto.getBackendConfig());
        target.setTag(dto.getTag());
    }

    public static void updateFrom(ProxyEndpointDto target, ProxyDto dto) {
        target.setProxyName(dto.getName());
        target.setProxyVersion(dto.getVersion());
    }
}
