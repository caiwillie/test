package com.brandnewdata.mop.poc.proxy.service.atomic;

import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyGroupDto;
import com.brandnewdata.mop.poc.proxy.dto.filter.ProxyFilter;

import java.util.List;
import java.util.Map;

public interface IProxyAService {

    Page<ProxyGroupDto> fetchPageGroupByName(Long projectId, Integer pageNum, Integer pageSize, String name, String tags);

    List<ProxyDto> fetchCacheListByFilter(ProxyFilter filter);

    ProxyDto save(ProxyDto proxy, boolean imported);

    Map<Long, ProxyDto> fetchById(List<Long> idList);

    ProxyDto fetchByDomain(String domain);

    ProxyDto changeState(ProxyDto proxyDto);

}
