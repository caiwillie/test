package com.brandnewdata.mop.poc.proxy.service.atomic;

import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.proxy.bo.ProxyFilter;
import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyGroupDto;

import java.util.List;
import java.util.Map;

public interface IProxyAService {

    Page<ProxyGroupDto> fetchPageGroupByName(Integer pageNum, Integer pageSize, String name, String tags);

    List<ProxyDto> fetchListByFilter(ProxyFilter filter);



    ProxyDto save(ProxyDto proxy, boolean imported);

    Map<Long, ProxyDto> fetchById(List<Long> idList);

    ProxyDto fetchByDomain(String domain);

}
