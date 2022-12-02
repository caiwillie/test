package com.brandnewdata.mop.poc.proxy.converter;

import com.brandnewdata.mop.poc.proxy.po.ProxyEndpointPo;

public class ProxyEndpointPoConverter {


    public static ProxyEndpointPo createFrom(com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto item) {
        ProxyEndpointPo po = new ProxyEndpointPo();
        po.setId(item.getId());
        po.setProxyId(item.getProxyId());
        po.setLocation(item.getLocation());
        po.setDescription(item.getDescription());
        po.setBackendType(item.getBackendType());
        po.setBackendConfig(item.getBackendConfig());
        po.setTag(item.getTag());
        return po;
    }


}
