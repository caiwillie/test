package com.brandnewdata.mop.poc.proxy.converter;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Opt;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointCallDto;
import com.brandnewdata.mop.poc.proxy.po.ProxyEndpointCallPo;

public class ProxyEndpointCallDtoConverter {
    public static ProxyEndpointCallDto createFrom(ProxyEndpointCallPo po) {
        ProxyEndpointCallDto dto = new ProxyEndpointCallDto();
        dto.setId(po.getId());
        dto.setCreateTime(Opt.of(po.getCreateTime()).map(LocalDateTimeUtil::of).orElse(null));
        dto.setUpdateTime(Opt.of(po.getUpdateTime()).map(LocalDateTimeUtil::of).orElse(null));
        dto.setEndpointId(po.getEndpointId());
        dto.setIp(po.getIp());
        dto.setMac(po.getMac());
        dto.setUserAgent(po.getUserAgent());
        dto.setHttpMethod(po.getHttpMethod());
        dto.setHttpStatus(po.getHttpStatus());
        dto.setHttpQuery(po.getHttpQuery());
        dto.setHttpBody(po.getHttpBody());
        dto.setTimeConsuming(po.getTimeConsuming());
        return dto;
    }

}
