package com.brandnewdata.mop.poc.proxy.converter;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;
import com.brandnewdata.mop.poc.proxy.po.ProxyPo;

public class ProxyDtoConverter {

    public static ProxyDto createFrom(ProxyPo po, String domainPattern) {
        if(po == null) return null;
        ProxyDto dto = new ProxyDto();
        dto.setId(po.getId());
        dto.setCreateTime(Opt.of(po.getCreateTime()).map(LocalDateTimeUtil::of).get());
        dto.setUpdateTime(Opt.of(po.getUpdateTime()).map(LocalDateTimeUtil::of).get());
        dto.setName(po.getName());
        dto.setProtocol(po.getProtocol());
        dto.setVersion(po.getVersion());
        dto.setDescription(po.getDescription());
        if(domainPattern != null) {
            // domainPattern 不为空，就生成 domain
            dto.setDomain(StrUtil.format(domainPattern, po.getDomain()));
        }
        dto.setTag(po.getTag());
        dto.setState(po.getState());
        return dto;
    }

}
