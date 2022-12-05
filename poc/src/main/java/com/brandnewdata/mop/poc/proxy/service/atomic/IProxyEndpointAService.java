package com.brandnewdata.mop.poc.proxy.service.atomic;

import com.brandnewdata.mop.poc.proxy.bo.ProxyEndpointServerBo;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto;

import java.util.List;
import java.util.Map;

public interface IProxyEndpointAService {

    ProxyEndpointDto fetchByProxyIdAndLocation(Long proxyId, String location);

    Map<Long, ProxyEndpointDto> fetchByIds(List<Long> ids);

    List<ProxyEndpointDto> fetchAll();

    ProxyEndpointServerBo parseServerConfig(String config);
}
