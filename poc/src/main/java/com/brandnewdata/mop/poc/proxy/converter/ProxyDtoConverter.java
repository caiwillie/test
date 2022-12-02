package com.brandnewdata.mop.poc.proxy.converter;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Opt;
import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;
import com.brandnewdata.mop.poc.proxy.po.ProxyPo;

public class ProxyDtoConverter {

    public static ProxyDto createFrom(ProxyPo po) {
        ProxyDto dto = new ProxyDto();
        dto.setId(po.getId());
        dto.setCreateTime(Opt.of(po.getCreateTime()).map(LocalDateTimeUtil::of).get());
        dto.setUpdateTime(Opt.of(po.getUpdateTime()).map(LocalDateTimeUtil::of).get());
        dto.setName(po.getName());
        dto.setProtocol(po.getProtocol());
        dto.setVersion(po.getVersion());
        dto.setDescription(po.getDescription());
        dto.setDomain(po.getDomain());
        dto.setTag(po.getTag());
        dto.setState(po.getState());
        return dto;
    }

}
