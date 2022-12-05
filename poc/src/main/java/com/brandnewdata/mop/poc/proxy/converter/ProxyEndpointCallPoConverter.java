package com.brandnewdata.mop.poc.proxy.converter;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Opt;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointCallDto;
import com.brandnewdata.mop.poc.proxy.po.ProxyEndpointCallPo;

public class ProxyEndpointCallPoConverter {
    public static ProxyEndpointCallPo createFrom(ProxyEndpointCallDto dto) {
        ProxyEndpointCallPo po = new ProxyEndpointCallPo();
        po.setId(dto.getId());
        po.setStartTime(Opt.of(dto.getStartTime()).map(DateUtil::date).get());
        po.setEndpointId(dto.getEndpointId());
        po.setIpAddress(dto.getIpAddress());
        po.setMacAddress(dto.getMacAddress());
        po.setUserAgent(dto.getUserAgent());
        po.setHttpMethod(dto.getHttpMethod());
        po.setHttpStatus(dto.getHttpStatus());
        po.setRequestQuery(dto.getRequestQuery());
        po.setRequestBody(dto.getRequestBody());
        po.setResponseBody(dto.getResponseBody());
        po.setErrorMessage(dto.getErrorMessage());
        po.setTimeConsuming(dto.getTimeConsuming());
        return po;
    }
}
