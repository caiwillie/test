package com.brandnewdata.mop.poc.proxy.service;

import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;

import java.util.List;
import java.util.Map;

public interface IProxyService2 {

    Map<Long, ProxyDto> fetchById(List<Long> idList);

}
