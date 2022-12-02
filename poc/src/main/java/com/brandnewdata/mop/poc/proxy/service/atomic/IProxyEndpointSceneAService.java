package com.brandnewdata.mop.poc.proxy.service.atomic;

import com.brandnewdata.mop.poc.proxy.bo.ProxyEndpointSceneBo;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointSceneDto;

public interface IProxyEndpointSceneAService {

    void save(Long endpointId, String config);

    ProxyEndpointSceneBo parseConfig(String config);
}
