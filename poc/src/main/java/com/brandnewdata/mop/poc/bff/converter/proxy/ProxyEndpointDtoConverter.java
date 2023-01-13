package com.brandnewdata.mop.poc.bff.converter.proxy;

import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.bff.vo.proxy.ProxyEndpointVo;
import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto;

public class ProxyEndpointDtoConverter {

    public static ProxyEndpointDto createFrom(ProxyEndpointVo vo) {
        ProxyEndpointDto dto = new ProxyEndpointDto();
        dto.setId(Opt.ofNullable(vo.getId()).map(Long::valueOf).orElse(null));
        dto.setProxyId(vo.getProxyId());
        dto.setLocation(vo.getLocation());
        dto.setBackendType(vo.getBackendType());
        dto.setBackendConfig(vo.getBackendConfig());
        dto.setDescription(StrUtil.trim(vo.getDescription()));
        dto.setTag(StrUtil.trim(vo.getTag()));
        return dto;
    }

    public static void updateFrom(ProxyEndpointDto target, ProxyDto dto) {
        if(dto == null) return;
        target.setProxyName(dto.getName());
        target.setProxyVersion(dto.getVersion());
    }
}
