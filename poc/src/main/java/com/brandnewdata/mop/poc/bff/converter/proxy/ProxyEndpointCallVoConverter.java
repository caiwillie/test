package com.brandnewdata.mop.poc.bff.converter.proxy;

import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.bff.vo.proxy.operate.ProxyEndpointCallVo;
import com.brandnewdata.mop.poc.constant.ProxyConst;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointCallDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto;

import java.util.ArrayList;
import java.util.List;

public class ProxyEndpointCallVoConverter {

    public static ProxyEndpointCallVo createFrom(ProxyEndpointCallDto dto, ProxyEndpointDto proxyEndpointDto) {
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
        vo.setProxyId(proxyEndpointDto.getProxyId());
        vo.setProxyName(proxyEndpointDto.getProxyName());
        vo.setLocation(proxyEndpointDto.getLocation());
        vo.setProxyVersion(proxyEndpointDto.getProxyVersion());
        vo.setHttpMethod(dto.getHttpMethod());
        vo.setExecuteStatus(StrUtil.equals(dto.getExecuteStatus(), ProxyConst.CALL_EXECUTE_STATUS__SUCCESS) ?
                ProxyConst.CALL_EXECUTE_STATUS__SUCCESS : ProxyConst.CALL_EXECUTE_STATUS__FAIL);
        vo.setRequestBody(dto.getRequestBody());
        vo.setErrorMessage(dto.getErrorMessage());
        vo.setTimeConsuming(dto.getTimeConsuming());
        return vo;
    }

}
