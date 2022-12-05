package com.brandnewdata.mop.poc.proxy.service.combined;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.constant.ProxyConst;
import com.brandnewdata.mop.poc.proxy.converter.ProxyEndpointDtoConverter;
import com.brandnewdata.mop.poc.proxy.converter.ProxyEndpointPoConverter;
import com.brandnewdata.mop.poc.proxy.dao.ProxyEndpointDao;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyEndpointAService;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyEndpointSceneAService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ProxyEndpointCService implements IProxyEndpointCService {

    @Resource
    private ProxyEndpointDao proxyEndpointDao;

    private final IProxyEndpointAService proxyEndpointAService;

    private final IProxyEndpointSceneAService proxyEndpointSceneService;

    public ProxyEndpointCService(IProxyEndpointAService proxyEndpointAService,
                                 IProxyEndpointSceneAService proxyEndpointSceneService) {
        this.proxyEndpointAService = proxyEndpointAService;
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
            ProxyEndpointDto proxyEndpointDto = proxyEndpointAService.fetchByIds(ListUtil.of(id)).get(id);
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


    private void checkPath(Long proxyId, String location) {
        // endpoint 的唯一性校验
        ProxyEndpointDto exist = proxyEndpointAService.fetchByProxyIdAndLocation(proxyId, location);
        Assert.isNull(exist, "路径 {} 已存在", location);
    }

    private void saveBackendConfig(Long id, Integer backendType, String backendConfig) {
        if(NumberUtil.equals(backendType, ProxyConst.BACKEND_TYPE__SCENE)) {
            // 场景
            proxyEndpointSceneService.save(id, backendConfig);
        } else if(NumberUtil.equals(backendType, ProxyConst.BACKEND_TYPE__SERVER)) {
            // 服务
            proxyEndpointAService.parseServerConfig(backendConfig);
        } else {
            throw new IllegalArgumentException("backend type not support");
        }
    }

}
