package com.brandnewdata.mop.poc.proxy.converter;

import cn.hutool.core.lang.Assert;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto;
import com.brandnewdata.mop.poc.proxy.po.ProxyEndpointPo;

public class ProxyEndpointPoConverter {


    public static ProxyEndpointPo createFrom(ProxyEndpointDto dto) {
        Assert.notNull(dto);
        ProxyEndpointPo po = new ProxyEndpointPo();
        po.setId(dto.getId());
        po.setProxyId(dto.getProxyId());
        po.setLocation(dto.getLocation());
        po.setDescription(dto.getDescription());
        po.setBackendType(dto.getBackendType());
        po.setBackendConfig(dto.getBackendConfig());
        po.setTag(dto.getTag());
        return po;
    }


}
