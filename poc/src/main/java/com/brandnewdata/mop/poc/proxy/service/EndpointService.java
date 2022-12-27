package com.brandnewdata.mop.poc.proxy.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.proxy.dao.ProxyEndpointDao;
import com.brandnewdata.mop.poc.proxy.dto.old.Endpoint;
import com.brandnewdata.mop.poc.proxy.po.ProxyEndpointPo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public Endpoint getOne(long id) {
        ProxyEndpointPo entity = endpointDao.selectById(id);
        return Optional.ofNullable(entity).map(this::toDTO).orElse(null);
    }

    public List<Endpoint> listByProxyIdList(List<Long> proxyIdList) {
        if(CollUtil.isEmpty(proxyIdList)) {
            return ListUtil.empty();
        }
        QueryWrapper<ProxyEndpointPo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(ProxyEndpointPo.PROXY_ID, proxyIdList);
        List<ProxyEndpointPo> list = endpointDao.selectList(queryWrapper);
        List<Endpoint> ret = list.stream().map(this::toDTO).collect(Collectors.toList());
        return ret;
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

    private Endpoint toDTO(ProxyEndpointPo entity) {
        Assert.notNull(entity);
        Endpoint dto = new Endpoint();
        dto.setId(String.valueOf(entity.getId()));
        dto.setProxyId(entity.getProxyId());
        dto.setLocation(entity.getLocation());
        dto.setDescription(entity.getDescription());
        dto.setBackendType(entity.getBackendType());
        dto.setBackendConfig(entity.getBackendConfig());
        dto.setTag(entity.getTag());
        return dto;
    }



}
