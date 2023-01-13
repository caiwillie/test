package com.brandnewdata.mop.poc.bff.converter.proxy;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.brandnewdata.mop.poc.bff.vo.proxy.ProxyVo;
import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;

public class ProxyVoConverter {

    public static ProxyVo createFrom(ProxyDto dto) {
        ProxyVo vo = new ProxyVo();
        vo.setId(dto.getId());
        vo.setCreateTime(dto.getCreateTime());
        vo.setUpdateTime(dto.getUpdateTime());
        vo.setName(dto.getName());
        vo.setDomain(dto.getDomain());
        vo.setVersion(dto.getVersion());
        vo.setProtocol(dto.getProtocol());
        vo.setDescription(dto.getDescription());
        vo.setTag(StringPool.EMPTY.equals(dto.getTag()) ? null : dto.getTag());
        vo.setState(dto.getState());
        return vo;
    }
}
