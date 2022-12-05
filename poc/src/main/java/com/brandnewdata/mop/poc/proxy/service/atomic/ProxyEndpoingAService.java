package com.brandnewdata.mop.poc.proxy.service.atomic;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.proxy.bo.ProxyEndpointServerBo;
import com.brandnewdata.mop.poc.proxy.converter.ProxyEndpointDtoConverter;
import com.brandnewdata.mop.poc.proxy.dao.ProxyEndpointDao;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto;
import com.brandnewdata.mop.poc.proxy.po.ProxyEndpointPo;
import com.dxy.library.json.jackson.JacksonUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProxyEndpoingAService implements IProxyEndpointAService {

    @Resource
    private ProxyEndpointDao proxyEndpointDao;

    @Override
    public ProxyEndpointDto fetchByProxyIdAndLocation(Long proxyId, String location) {
        // proxy id not null
        Assert.notNull(proxyId, "proxy id not null");
        Assert.notNull(location, "location not null");

        QueryWrapper<ProxyEndpointPo> query = new QueryWrapper<>();
        query.eq(ProxyEndpointPo.PROXY_ID, proxyId);
        query.eq(ProxyEndpointPo.LOCATION, location);

        ProxyEndpointPo proxyEndpointPo = proxyEndpointDao.selectOne(query);
        return proxyEndpointPo == null ? null : ProxyEndpointDtoConverter.createFrom(proxyEndpointPo);
    }

    @Override
    public Map<Long, ProxyEndpointDto> fetchByIds(List<Long> idList) {
        if(CollUtil.isEmpty(idList)) return MapUtil.empty();
        Assert.isFalse(CollUtil.hasNull(idList), "idList can not contain null");

        QueryWrapper<ProxyEndpointPo> query = new QueryWrapper<>();
        query.in(ProxyEndpointPo.ID, idList);
        List<ProxyEndpointPo> proxyEndpointPos = proxyEndpointDao.selectList(query);

        return proxyEndpointPos.stream().map(ProxyEndpointDtoConverter::createFrom)
                .collect(Collectors.toMap(com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto::getId, Function.identity()));
    }

    @Override
    public List<ProxyEndpointDto> fetchAll() {
        QueryWrapper<ProxyEndpointPo> query = new QueryWrapper<>();
        List<ProxyEndpointPo> list = proxyEndpointDao.selectList(query);
        return list.stream().map(ProxyEndpointDtoConverter::createFrom).collect(Collectors.toList());
    }

    @Override
    public ProxyEndpointServerBo parseServerConfig(String config) {
        ProxyEndpointServerBo bo = JacksonUtil.from(config, ProxyEndpointServerBo.class);
        Assert.notNull(bo.getBaseUrl(), "服务地址不能为空");
        return bo;
    }
}
