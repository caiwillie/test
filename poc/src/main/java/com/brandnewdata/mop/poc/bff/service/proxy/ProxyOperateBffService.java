package com.brandnewdata.mop.poc.bff.service.proxy;

import com.brandnewdata.mop.poc.bff.vo.proxy.operate.ProxyEndpointCallFilter;
import com.brandnewdata.mop.poc.bff.vo.proxy.operate.ProxyEndpointCallVo;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.proxy.service.IProxyEndpointService2;
import org.springframework.stereotype.Service;

@Service
public class ProxyOperateBffService {

    private final IProxyEndpointService2 proxyEndpointService;

    public ProxyOperateBffService(IProxyEndpointService2 proxyEndpointService) {
        this.proxyEndpointService = proxyEndpointService;
    }

    public Page<ProxyEndpointCallVo> page(ProxyEndpointCallFilter filter) {

    }

}
