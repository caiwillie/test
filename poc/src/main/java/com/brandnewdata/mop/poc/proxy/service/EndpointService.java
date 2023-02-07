package com.brandnewdata.mop.poc.proxy.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.proxy.dao.ProxyEndpointDao;
import com.brandnewdata.mop.poc.proxy.dto.old.Endpoint;
import com.brandnewdata.mop.poc.proxy.po.ProxyEndpointPo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author caiwillie
 */
@Service
public class EndpointService {

    @Resource
    private ProxyEndpointDao endpointDao;

    public Endpoint save(Endpoint endpoint) {
        ProxyEndpointPo entity = toEntity(endpoint);
        Long id = entity.getId();
        if(id == null) {
            // endpoint 的唯一性校验
            ProxyEndpointPo exist = exist(entity.getProxyId(), entity.getLocation());
            Assert.isNull(exist, "路径 {} 已存在", endpoint.getLocation());

            endpointDao.insert(entity);
            endpoint.setId(String.valueOf(entity.getId()));
        } else {
            ProxyEndpointPo oldEntity = endpointDao.selectById(id);

            // 将新对象的值拷贝到旧对象，排除掉 proxyId
            BeanUtil.copyProperties(entity, oldEntity, "proxyId");
            endpointDao.updateById(oldEntity);
        }
        return endpoint;
    }

    private ProxyEndpointPo exist(Long proxyId, String location) {
        QueryWrapper<ProxyEndpointPo> query = new QueryWrapper<>();
        query.eq(ProxyEndpointPo.PROXY_ID, proxyId);
        query.eq(ProxyEndpointPo.LOCATION, location);
        return endpointDao.selectOne(query);
    }



    private ProxyEndpointPo toEntity(Endpoint dto) {
        Assert.notNull(dto);
        ProxyEndpointPo entity = new ProxyEndpointPo();
        entity.setId(Long.parseLong(dto.getId()));
        entity.setProxyId(dto.getProxyId());
        entity.setLocation(dto.getLocation());
        entity.setDescription(dto.getDescription());
        entity.setBackendType(dto.getBackendType());
        entity.setBackendConfig(dto.getBackendConfig());
        entity.setTag(dto.getTag());
        return entity;
    }

}
