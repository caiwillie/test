package com.brandnewdata.mop.poc.proxy.service.combined;

import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointCallDto;

import java.util.List;

public interface IProxyEndpointCallCService {

    Page<ProxyEndpointCallDto> pageByEndpointId(Integer pageNum, Integer pageSize, List<Long> endpointIdList);

}
