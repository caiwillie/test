package com.brandnewdata.mop.poc.proxy.service;

import cn.hutool.core.lang.Assert;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.proxy.dao.ReverseProxyDao;
import com.brandnewdata.mop.poc.proxy.dto.Proxy;
import com.brandnewdata.mop.poc.proxy.entity.ReverseProxyEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * @author caiwillie
 */
@Service
public class ReverseProxyService {

    @Resource
    private ReverseProxyDao reverseProxyDao;


    public Proxy getOne(long id) {
        ReverseProxyEntity reverseProxyEntity = reverseProxyDao.selectById(id);
        return Optional.ofNullable(reverseProxyEntity).map(this::toDTO).orElse(null);
    }

    public Page<Proxy> page(int pageNum, int pageSize) {
        Assert.isTrue(pageNum > 1);
        Assert.isTrue(pageSize > 1);
        return null;
    }

    private Proxy toDTO(ReverseProxyEntity entity) {
        Proxy proxy = new Proxy();
        proxy.setId(entity.getId());
        proxy.setName(entity.getName());
        proxy.setProtocol(entity.getProtocol());
        proxy.setVersion(entity.getVersion());
        proxy.setDescription(entity.getDescription());
        return proxy;
    }

}
