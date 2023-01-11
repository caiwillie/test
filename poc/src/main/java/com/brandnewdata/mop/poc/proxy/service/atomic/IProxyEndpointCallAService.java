package com.brandnewdata.mop.poc.proxy.service.atomic;

import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointCallDto;
import com.brandnewdata.mop.poc.proxy.dto.filter.ProxyEndpointCallFilter;

import java.util.List;
import java.util.Map;

public interface IProxyEndpointCallAService {

    Page<ProxyEndpointCallDto> fetchPageByEndpointId(Integer pageNum, Integer pageSize, List<Long> endpointIdList);

    ProxyEndpointCallDto save(ProxyEndpointCallDto dto);

    Map<Long, List<ProxyEndpointCallDto>> fetchCacheListByEndpointId(List<Long> endpointIdList, ProxyEndpointCallFilter filter);

}
