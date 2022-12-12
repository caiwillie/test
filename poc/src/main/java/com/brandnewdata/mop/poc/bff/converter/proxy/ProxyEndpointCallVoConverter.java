package com.brandnewdata.mop.poc.bff.converter.proxy;

import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.bff.vo.proxy.operate.ProxyEndpointCallVo;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointCallDto;

import java.util.ArrayList;
import java.util.List;

public class ProxyEndpointCallVoConverter {

    public static ProxyEndpointCallVo createFrom(ProxyEndpointCallDto dto) {
        ProxyEndpointCallVo vo = new ProxyEndpointCallVo();
        vo.setId(String.valueOf(dto.getId()));
        vo.setCreateTime(dto.getCreateTime());
        vo.setUpdateTime(dto.getUpdateTime());
        // 设置source
        List<String> sourceList = new ArrayList<>();
        if(StrUtil.isNotBlank(dto.getIpAddress())) {
            sourceList.add(dto.getIpAddress());
        }
        if(StrUtil.isNotBlank(dto.getUserAgent())) {
            sourceList.add(dto.getUserAgent());
        }
        vo.setSource(StrUtil.join(" / ",sourceList));

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
