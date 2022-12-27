package com.brandnewdata.mop.poc.proxy.service.combined;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.brandnewdata.mop.poc.constant.ProxyConst;
import com.brandnewdata.mop.poc.proxy.dao.ProxyDao;
import com.brandnewdata.mop.poc.proxy.po.ProxyPo;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyEndpointAService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ProxyCService implements IProxyCService {

    @Resource
    private ProxyDao proxyDao;

    private final IProxyEndpointAService proxyEndpointAService;

    public ProxyCService(IProxyEndpointAService proxyEndpointAService) {
        this.proxyEndpointAService = proxyEndpointAService;
    }

    @Override
    public void deleteById(Long id) {
        Assert.notNull(id, "proxy id must not null");
        ProxyPo proxyPo = proxyDao.selectById(id);
        Assert.notNull(proxyPo, "proxy id not exist: {}", id);
        Integer state = proxyPo.getState();
        Assert.isTrue(NumberUtil.equals(ProxyConst.PROXY_STATE__STOPPED, state), "api状态异常");

        UpdateWrapper<ProxyPo> update = new UpdateWrapper<>();
        update.setSql(StrUtil.format("{} = {}", ProxyPo.DELETE_FLAG, ProxyPo.ID));
        update.eq(ProxyPo.ID, id);
        proxyDao.update(null, update);
        proxyEndpointAService.deleteByProxyId(id);
    }
}
