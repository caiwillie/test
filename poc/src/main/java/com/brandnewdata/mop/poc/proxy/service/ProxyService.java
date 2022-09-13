package com.brandnewdata.mop.poc.proxy.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.proxy.dao.ReverseProxyDao;
import com.brandnewdata.mop.poc.proxy.dto.Proxy;
import com.brandnewdata.mop.poc.proxy.entity.ReverseProxyEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
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

    @Value("${brandnewdata.api.suffixDomain}")
    private String apiSuffixDomain;

    @PostConstruct
    public void postConstruct() {
        return;
    }

    public Proxy save(Proxy proxy) {
        ReverseProxyEntity entity = toEntity(proxy);
        Long id = entity.getId();
        if(id == null) {
            String name = proxy.getName();
            String version = proxy.getVersion();
            String domain = StrUtil.format("api.{}.{}",
                    DigestUtil.md5Hex(StrUtil.format("{}:{}", name, version)), apiSuffixDomain);
            entity.setDomain(domain);
            // 判断是否唯一
            ReverseProxyEntity exist = exist(name, version);
            Assert.isNull(exist, "api 已存在");
            proxyDao.insert(entity);
            proxy.setId(entity.getId());
        } else {
            Proxy oldOne = getOne(id);
            // 将新对象的值拷贝到旧对象
            BeanUtil.copyProperties(entity, oldOne);
            proxyDao.updateById(entity);
        }
        return proxy;
    }

    private ReverseProxyEntity exist(String name, String version) {
        QueryWrapper<ReverseProxyEntity> query = new QueryWrapper<>();
        query.eq(ReverseProxyEntity.NAME, name);
        query.eq(ReverseProxyEntity.VERSION, version);
        return proxyDao.selectOne(query);
    }

    public Proxy getOne(long id) {
        ReverseProxyEntity reverseProxyEntity = proxyDao.selectById(id);
        return Optional.ofNullable(reverseProxyEntity).map(this::toDTO).orElse(null);
    }

    public Page<Proxy> page(int pageNum, int pageSize) {
        Assert.isTrue(pageNum > 0);
        Assert.isTrue(pageSize > 0);
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
        proxy.setCreateTime(LocalDateTimeUtil.of(entity.getCreateTime()));
        proxy.setUpdateTime(LocalDateTimeUtil.of(entity.getUpdateTime()));
        proxy.setDomain(entity.getDomain());
        return proxy;
    }

    private ReverseProxyEntity toEntity(Proxy proxy) {
        Assert.notNull(proxy);
        ReverseProxyEntity entity = new ReverseProxyEntity();
        entity.setId(proxy.getId());
        entity.setName(proxy.getName());
        entity.setProtocol(proxy.getProtocol());
        entity.setVersion(proxy.getVersion());
        entity.setDescription(proxy.getDescription());
        return entity;
    }

}
