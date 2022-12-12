package com.brandnewdata.mop.poc.proxy.converter;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Opt;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointCallDto;
import com.brandnewdata.mop.poc.proxy.po.ProxyEndpointCallPo;

public class ProxyEndpointCallDtoConverter {
    public static ProxyEndpointCallDto createFrom(ProxyEndpointCallPo po) {
        if(po == null) return null;
        ProxyEndpointCallDto dto = new ProxyEndpointCallDto();
        dto.setId(po.getId());
        dto.setCreateTime(Opt.of(po.getCreateTime()).map(LocalDateTimeUtil::of).orElse(null));
        dto.setUpdateTime(Opt.of(po.getUpdateTime()).map(LocalDateTimeUtil::of).orElse(null));
        dto.setStartTime(Opt.of(po.getStartTime()).map(LocalDateTimeUtil::of).orElse(null));
        dto.setEndpointId(po.getEndpointId());
        dto.setIpAddress(po.getIpAddress());
        dto.setMacAddress(po.getMacAddress());
        dto.setUserAgent(po.getUserAgent());
        dto.setHttpMethod(po.getHttpMethod());
        dto.setExecuteStatus(po.getExecuteStatus());
        dto.setRequestQuery(po.getRequestQuery());
        dto.setRequestBody(po.getRequestBody());
        dto.setResponseBody(po.getResponseBody());
        dto.setErrorMessage(po.getErrorMessage());
        dto.setTimeConsuming(po.getTimeConsuming());
        return dto;
    }

}
