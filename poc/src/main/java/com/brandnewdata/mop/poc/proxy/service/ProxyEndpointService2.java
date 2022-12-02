package com.brandnewdata.mop.poc.proxy.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.constant.ProxyConst;
import com.brandnewdata.mop.poc.proxy.converter.ProxyEndpointDtoConverter;
import com.brandnewdata.mop.poc.proxy.converter.ProxyEndpointPoConverter;
import com.brandnewdata.mop.poc.proxy.dao.ProxyEndpointDao;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto;
import com.brandnewdata.mop.poc.proxy.po.ProxyEndpointPo;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProxyEndpointService2 implements IProxyEndpointService2 {

    @Resource
    private ProxyEndpointDao proxyEndpointDao;

    private final IProxyEndpointSceneService proxyEndpointSceneService;

    public ProxyEndpointService2(IProxyEndpointSceneService proxyEndpointSceneService) {
        this.proxyEndpointSceneService = proxyEndpointSceneService;
    }

    @Override
    public ProxyEndpointDto save(ProxyEndpointDto dto) {
        Long id = dto.getId();
        Long proxyId = dto.getProxyId();
        String location = dto.getLocation();
        Integer backendType = dto.getBackendType();
        String backendConfig = dto.getBackendConfig();

        Assert.notNull(proxyId);
        Assert.notNull(location);
        Assert.notNull(backendType);
        Assert.notNull(backendConfig);

        if (id == null) {
            // save
            // endpoint 的唯一性校验
            checkPath(proxyId, location);

            dto.setId(IdUtil.getSnowflakeNextId());
            proxyEndpointDao.insert(ProxyEndpointPoConverter.createFrom(dto));
        } else {
            // update
            ProxyEndpointDto proxyEndpointDto = fetchByIds(ListUtil.of(id)).get(id);
            String oldLocation = proxyEndpointDto.getLocation();
            if(!StrUtil.equals(location, oldLocation)) {
                // endpoint 的唯一性校验
                checkPath(proxyId, location);
            }

            ProxyEndpointDtoConverter.updateFrom(proxyEndpointDto, dto);
            proxyEndpointDao.updateById(ProxyEndpointPoConverter.createFrom(proxyEndpointDto));
        }


        // 根据配置类型单独处理
        saveBackendConfig(dto.getId(), backendType, backendConfig);

        return dto;
    }

    @Override
    public com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto fetchByProxyIdAndLocation(Long proxyId, String location) {
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

    private void checkPath(Long proxyId, String location) {
        // endpoint 的唯一性校验
        ProxyEndpointDto exist = fetchByProxyIdAndLocation(proxyId, location);
        Assert.isNull(exist, "路径 {} 已存在", location);
    }

    private void saveBackendConfig(Long id, Integer backendType, String backendConfig) {
        if(NumberUtil.equals(backendType, ProxyConst.BACKEND_TYPE__SCENE)) {
            // 场景
            proxyEndpointSceneService.save(id, backendConfig);
        } else if(NumberUtil.equals(backendType, ProxyConst.BACKEND_TYPE__SERVER)) {
            // 服务
        } else {
            throw new IllegalArgumentException("backend type not support");
        }
    }

}
