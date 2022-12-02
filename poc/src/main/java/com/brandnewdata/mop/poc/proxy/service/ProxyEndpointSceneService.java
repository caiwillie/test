package com.brandnewdata.mop.poc.proxy.service;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.proxy.bo.ProxyEndpointSceneBo;
import com.brandnewdata.mop.poc.proxy.converter.ProxyEndpointScenePoConverter;
import com.brandnewdata.mop.poc.proxy.dao.ProxyEndpointSceneDao;
import com.brandnewdata.mop.poc.proxy.po.ProxyEndpointScenePo;
import com.dxy.library.json.jackson.JacksonUtil;

import javax.annotation.Resource;

public class ProxyEndpointSceneService implements IProxyEndpointSceneService{

    @Resource
    private ProxyEndpointSceneDao proxyEndpointSceneDao;

    @Override
    public void save(Long endpointId, String config) {
        ProxyEndpointScenePo proxyEndpointScenePo = fetchByEndpointId(endpointId);
        Assert.notNull(config, "场景配置不能为空");
        ProxyEndpointSceneBo bo = JacksonUtil.from(config, ProxyEndpointSceneBo.class);

        Assert.notNull(bo.getEnvId(), "环境ID不能为空");
        Assert.notNull(bo.getEnvName(), "环境名称不能为空");
        Assert.notNull(bo.getSceneId(), "场景ID不能为空");
        Assert.notNull(bo.getSceneName(), "场景名称不能为空");
        Assert.notNull(bo.getProcessId(), "流程ID不能为空");
        Assert.notNull(bo.getProcessName(), "流程名称不能为空");

        if(proxyEndpointScenePo == null) {
            proxyEndpointScenePo = new ProxyEndpointScenePo();
            proxyEndpointScenePo.setEndpointId(endpointId);
            ProxyEndpointScenePoConverter.updateFrom(proxyEndpointScenePo, bo);
            proxyEndpointSceneDao.insert(proxyEndpointScenePo);
        } else {
            ProxyEndpointScenePoConverter.updateFrom(proxyEndpointScenePo, bo);
            proxyEndpointSceneDao.updateById(proxyEndpointScenePo);
        }
    }

    private ProxyEndpointScenePo fetchByEndpointId(Long endpointId) {
        Assert.notNull(endpointId, "endpointId is null");
        QueryWrapper<ProxyEndpointScenePo> query = new QueryWrapper<>();
        query.eq(ProxyEndpointScenePo.ENDPOINT_ID, endpointId);

        return proxyEndpointSceneDao.selectOne(query);
    }

}
