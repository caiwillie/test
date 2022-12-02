package com.brandnewdata.mop.poc.bff.service.proxy;

import com.brandnewdata.mop.poc.bff.converter.proxy.ProxyEndpointDtoConverter;
import com.brandnewdata.mop.poc.bff.converter.proxy.ProxyEndpointVoConverter;
import com.brandnewdata.mop.poc.bff.vo.proxy.ProxyEndpointVo;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto;
import com.brandnewdata.mop.poc.proxy.service.IProxyEndpointService2;
import org.springframework.stereotype.Service;

@Service
public class ProxyBffService {

    private final IProxyEndpointService2 proxyEndpointService2;

    public ProxyBffService(IProxyEndpointService2 proxyEndpointService2) {
        this.proxyEndpointService2 = proxyEndpointService2;
    }

    public ProxyEndpointVo save(ProxyEndpointVo vo) {
        ProxyEndpointDto dto = proxyEndpointService2.save(ProxyEndpointDtoConverter.createFrom(vo));
        return ProxyEndpointVoConverter.createFrom(dto);
    }

}
