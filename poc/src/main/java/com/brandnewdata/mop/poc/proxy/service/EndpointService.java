package com.brandnewdata.mop.poc.proxy.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.proxy.dao.ReverseProxyEndpointDao;
import com.brandnewdata.mop.poc.proxy.dto.Endpoint;
import com.brandnewdata.mop.poc.proxy.dto.Proxy;
import com.brandnewdata.mop.poc.proxy.entity.ReverseProxyEndpointEntity;
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
    private ReverseProxyEndpointDao endpointDao;

    public Endpoint save(Endpoint endpoint) {
        ReverseProxyEndpointEntity entity = toEntity(endpoint);
        Long id = entity.getId();
        if(id == null) {
            // 唯一性校验
            ReverseProxyEndpointEntity exist = exist(entity.getProxyId(), entity.getLocation());
            Assert.isNull(exist, "路径 {} 已存在", endpoint.getLocation());

            endpointDao.insert(entity);
            endpoint.setId(entity.getId());
        } else {
            ReverseProxyEndpointEntity oldEntity = endpointDao.selectById(id);

            // 将新对象的值拷贝到旧对象，排除掉 proxyId
            BeanUtil.copyProperties(entity, oldEntity, "proxyId");
            endpointDao.updateById(entity);
        }
        return endpoint;
    }

    private ReverseProxyEndpointEntity exist(Long proxyId, String location) {
        QueryWrapper<ReverseProxyEndpointEntity> query = new QueryWrapper<>();
        query.eq(ReverseProxyEndpointEntity.PROXY_ID, proxyId);
        query.eq(ReverseProxyEndpointEntity.LOCATION, location);
        return endpointDao.selectOne(query);
    }

    public Endpoint getOne(long id) {
        ReverseProxyEndpointEntity entity = endpointDao.selectById(id);
        return Optional.ofNullable(entity).map(this::toDTO).orElse(null);
    }

    public Page<Endpoint> page(Long proxyId, int pageNum, int pageSize) {
        Assert.isTrue(pageNum > 0);
        Assert.isTrue(pageSize > 0);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ReverseProxyEndpointEntity> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        QueryWrapper<ReverseProxyEndpointEntity> queryWrapper = new QueryWrapper<>();
        if(proxyId != null) queryWrapper.eq(ReverseProxyEndpointEntity.PROXY_ID, proxyId);
        page = endpointDao.selectPage(page, queryWrapper);
        List<Endpoint> records = Optional.ofNullable(page.getRecords()).orElse(ListUtil.empty())
                .stream().map(this::toDTO).collect(Collectors.toList());
        return new Page<>(page.getTotal(), records);
    }

    public void deleteByIdList(List<Long> idList) {
        if(CollUtil.isEmpty(idList)) {
            return;
        }
        // 根据 id list 删除
        endpointDao.deleteBatchIds(idList);
    }

    public List<Endpoint> listByProxyIdList(List<Long> proxyIdList) {
        if(CollUtil.isEmpty(proxyIdList)) {
            return ListUtil.empty();
        }
        QueryWrapper<ReverseProxyEndpointEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(ReverseProxyEndpointEntity.PROXY_ID, proxyIdList);
        List<ReverseProxyEndpointEntity> list = endpointDao.selectList(queryWrapper);
        List<Endpoint> ret = list.stream().map(this::toDTO).collect(Collectors.toList());
        return ret;
    }

    private ReverseProxyEndpointEntity toEntity(Endpoint dto) {
        Assert.notNull(dto);
        ReverseProxyEndpointEntity entity = new ReverseProxyEndpointEntity();
        entity.setId(dto.getId());
        entity.setProxyId(dto.getProxyId());
        entity.setLocation(dto.getLocation());
        entity.setDescription(dto.getDescription());
        entity.setBackendType(dto.getBackendType());
        entity.setBackendConfig(dto.getBackendConfig());
        return entity;
    }

    private Endpoint toDTO(ReverseProxyEndpointEntity entity) {
        Assert.notNull(entity);
        Endpoint dto = new Endpoint();
        dto.setId(entity.getId());
        dto.setProxyId(entity.getProxyId());
        dto.setLocation(entity.getLocation());
        dto.setDescription(entity.getDescription());
        dto.setBackendType(entity.getBackendType());
        dto.setBackendConfig(entity.getBackendConfig());
        return dto;
    }



}
