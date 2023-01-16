package com.brandnewdata.mop.poc.proxy.service.atomic;

import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.proxy.bo.ProxyEndpointServerBo;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto;
import com.brandnewdata.mop.poc.proxy.dto.filter.ProxyEndpointFilter;

import java.util.List;
import java.util.Map;

public interface IProxyEndpointAService {

    Page<ProxyEndpointDto> pageByProxyId(Integer pageNum, Integer pageSize, Long proxyId);

    ProxyEndpointDto fetchByProxyIdAndLocation(Long proxyId, String location);

    Map<Long, ProxyEndpointDto> fetchByIds(List<Long> ids);

    Map<Long, List<ProxyEndpointDto>> fetchListByProxyIdAndFilter(List<Long> proxyIdList, ProxyEndpointFilter filter);

    ProxyEndpointServerBo parseServerConfig(String config);

    void deleteByProxyId(Long proxyId);

    void deleteById(Long id);

    List<String> listTag(Long proxyId);

    Map<Long, Integer> countByProxyId(List<Long> proxyIdList);
}
