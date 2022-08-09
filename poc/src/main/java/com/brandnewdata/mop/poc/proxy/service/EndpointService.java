package com.brandnewdata.mop.poc.proxy.service;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.proxy.dao.ReverseProxyEndpointDao;
import com.brandnewdata.mop.poc.proxy.dto.Endpoint;
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
        if(entity.getId() == null) {
            endpointDao.insert(entity);
            endpoint.setId(entity.getId());
        } else {
            endpointDao.updateById(entity);
        }
        return endpoint;
    }


    public Endpoint getOne(long id) {
        ReverseProxyEndpointEntity entity = endpointDao.selectById(id);
        return Optional.ofNullable(entity).map(this::toDTO).orElse(null);
    }


    public Page<Endpoint> page(int pageNum, int pageSize) {
        Assert.isTrue(pageNum > 0);
        Assert.isTrue(pageSize > 0);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ReverseProxyEndpointEntity> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        QueryWrapper<ReverseProxyEndpointEntity> queryWrapper = new QueryWrapper<>();
        page = endpointDao.selectPage(page, queryWrapper);
        List<Endpoint> records = Optional.ofNullable(page.getRecords()).orElse(ListUtil.empty())
                .stream().map(this::toDTO).collect(Collectors.toList());
        return new Page<>(page.getTotal(), records);
    }

    private ReverseProxyEndpointEntity toEntity(Endpoint dto) {
        Assert.notNull(dto);
        ReverseProxyEndpointEntity entity = new ReverseProxyEndpointEntity();
        entity.setId(dto.getId());
        entity.setProxyId(dto.getProxyId());
        entity.setLocation(dto.getLocation());
        entity.setDescription(dto.getDescription());
        return entity;
    }

    private Endpoint toDTO(ReverseProxyEndpointEntity entity) {
        Assert.notNull(entity);
        Endpoint dto = new Endpoint();
        dto.setId(entity.getId());
        dto.setProxyId(entity.getProxyId());
        dto.setLocation(entity.getLocation());
        dto.setDescription(entity.getDescription());
        return dto;
    }



}
