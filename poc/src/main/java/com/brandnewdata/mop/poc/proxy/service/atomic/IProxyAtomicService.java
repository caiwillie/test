package com.brandnewdata.mop.poc.proxy.service.atomic;

import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;

import java.util.List;
import java.util.Map;

public interface IProxyAtomicService {

    Map<Long, ProxyDto> fetchById(List<Long> idList);

}
