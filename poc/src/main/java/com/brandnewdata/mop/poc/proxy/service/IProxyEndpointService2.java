package com.brandnewdata.mop.poc.proxy.service;

import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto;

import java.util.List;
import java.util.Map;

public interface IProxyEndpointService2 {

    ProxyEndpointDto save(ProxyEndpointDto dto);

    ProxyEndpointDto fetchByProxyIdAndLocation(Long proxyId, String location);

    Map<Long, ProxyEndpointDto> fetchByIds(List<Long> ids);

    List<ProxyEndpointDto> fetchAll();

}
