package com.brandnewdata.mop.poc.proxy.converter;

import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;
import com.brandnewdata.mop.poc.proxy.po.ProxyPo;

public class ProxyPoConverter {
    public static ProxyPo createFrom(ProxyDto dto) {
        ProxyPo po = new ProxyPo();
        po.setId(dto.getId());
        po.setName(dto.getName());
        po.setProtocol(dto.getProtocol());
        po.setVersion(dto.getVersion());
        po.setDescription(dto.getDescription());
        po.setDomain(dto.getDomain());
        po.setTag(dto.getTag());
        po.setState(dto.getState());
        return po;
    }

    public static void updateFrom(ProxyDto target, ProxyDto dto) {
        target.setProtocol(dto.getProtocol());
        target.setDescription(dto.getDescription());
        target.setTag(dto.getTag());
        target.setState(dto.getState());
    }
}