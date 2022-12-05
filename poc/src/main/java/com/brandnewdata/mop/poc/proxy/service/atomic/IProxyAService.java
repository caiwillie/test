package com.brandnewdata.mop.poc.proxy.service.atomic;

import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyGroupDto;

import java.util.List;
import java.util.Map;

public interface IProxyAService {

    Page<ProxyGroupDto> pageGroupByName(Integer pageNum, Integer pageSize, String name, String tags);

    ProxyDto save(ProxyDto proxy, boolean imported);

    Map<Long, ProxyDto> fetchById(List<Long> idList);

    ProxyDto fetchByDomain(String domain);

}
