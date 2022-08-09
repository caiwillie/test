package com.brandnewdata.mop.poc.proxy.service;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.proxy.dao.ReverseProxyDao;
import com.brandnewdata.mop.poc.proxy.dto.Proxy;
import com.brandnewdata.mop.poc.proxy.entity.ReverseProxyEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author caiwillie
 */
@Service
public class ProxyService {

    @Resource
    private ReverseProxyDao proxyDao;

    public Proxy save(Proxy proxy) {
        ReverseProxyEntity entity = toEntity(proxy);
        if(entity.getId() == null) {
            proxyDao.insert(entity);
            proxy.setId(entity.getId());
        } else {
            proxyDao.updateById(entity);
        }
        return proxy;
    }

    public Proxy getOne(long id) {
        ReverseProxyEntity reverseProxyEntity = proxyDao.selectById(id);
        return Optional.ofNullable(reverseProxyEntity).map(this::toDTO).orElse(null);
    }

    public Page<Proxy> page(int pageNum, int pageSize) {
        Assert.isTrue(pageNum > 1);
        Assert.isTrue(pageSize > 1);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ReverseProxyEntity> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        QueryWrapper<ReverseProxyEntity> queryWrapper = new QueryWrapper<>();
        page = proxyDao.selectPage(page, queryWrapper);
        List<Proxy> records = Optional.ofNullable(page.getRecords()).orElse(ListUtil.empty())
                .stream().map(this::toDTO).collect(Collectors.toList());
        return new Page<>(page.getTotal(), records);
    }

    private Proxy toDTO(ReverseProxyEntity entity) {
        if(entity == null) return null;
        Proxy proxy = new Proxy();
        proxy.setId(entity.getId());
        proxy.setName(entity.getName());
        proxy.setProtocol(entity.getProtocol());
        proxy.setVersion(entity.getVersion());
        proxy.setDescription(entity.getDescription());
        return proxy;
    }

    private ReverseProxyEntity toEntity(Proxy proxy) {
        Assert.notNull(proxy);
        ReverseProxyEntity entity = new ReverseProxyEntity();
        entity.setId(proxy.getId());
        entity.setName(proxy.getName());
        entity.setProtocol(proxy.getProtocol());
        entity.setVersion(proxy.getVersion());
        return entity;
    }

}
