package com.brandnewdata.mop.poc.proxy.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.proxy.ProxyConstants;
import com.brandnewdata.mop.poc.proxy.dao.ReverseProxyDao;
import com.brandnewdata.mop.poc.proxy.dto.Endpoint;
import com.brandnewdata.mop.poc.proxy.dto.Proxy;
import com.brandnewdata.mop.poc.proxy.entity.ReverseProxyEntity;
import com.brandnewdata.mop.poc.proxy.resp.ApiResp;
import com.brandnewdata.mop.poc.proxy.resp.VersionSpecifiedResp;
import com.brandnewdata.mop.poc.util.PageEnhancedUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author caiwillie
 */
@Service
public class ProxyService {

    @Resource
    private ReverseProxyDao proxyDao;

    @Resource
    private EndpointService endpointService;

    @Value("${brandnewdata.api.suffixDomain}")
    private String apiSuffixDomain;

    private static VersionComparator VERSION_COMPARATOR = new VersionComparator();

    public Proxy save(Proxy proxy) {
        ReverseProxyEntity entity = toEntity(proxy);
        Long id = entity.getId();
        if(id == null) {
            String name = proxy.getName();
            String version = proxy.getVersion();
            // 判断 名称和版本 是否唯一
            ReverseProxyEntity exist = exist(name, version);
            Assert.isNull(exist, "版本 {} 已存在", name, version);
            String domain = StrUtil.format("api.{}.{}",
                    DigestUtil.md5Hex(StrUtil.format("{}:{}", name, version)), apiSuffixDomain);
            entity.setDomain(domain);
            // 设置默认状态是 停止
            entity.setState(ProxyConstants.STATE_STOP);
            proxyDao.insert(entity);
            proxy.setId(entity.getId());
        } else {
            ReverseProxyEntity oldEntity = proxyDao.selectById(id);
            // 将新对象的值拷贝到旧对象，排除掉 state 字段
            BeanUtil.copyProperties(entity, oldEntity, "state");
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

    public Page<ApiResp> pageV2(int pageNum, int pageSize, String name, String tags) {
        QueryWrapper<ReverseProxyEntity> queryWrapper = new QueryWrapper<>();
        if(StrUtil.isNotBlank(name)) {
            queryWrapper.like(ReverseProxyEntity.NAME, name);
        }

        if(StrUtil.isNotBlank(tags)) {
            String[] tagArr = tags.split(",");
            queryWrapper.in(ReverseProxyEntity.TAG, tagArr);
        }

        List<ReverseProxyEntity> entities = proxyDao.selectList(queryWrapper);
        Map<String, List<ReverseProxyEntity>> nameMap = new HashMap<>();
        if(CollUtil.isEmpty(entities)) {
            return new Page<>(0, ListUtil.empty());
        }

        for (ReverseProxyEntity entity : entities) {
            List<ReverseProxyEntity> list = nameMap.computeIfAbsent(entity.getName(), key -> new ArrayList<>());
            list.add(entity);
        }

        // 对nameMap进行排序
        List<Map.Entry<String, List<ReverseProxyEntity>>> sortedList = nameMap.entrySet().stream().sorted((o1, o2) -> {
            List<ReverseProxyEntity> list1 = o1.getValue();
            list1.sort(VERSION_COMPARATOR);
            List<ReverseProxyEntity> list2 = o2.getValue();
            list2.sort(VERSION_COMPARATOR);
            Date date1 = Optional.ofNullable(list1.get(0).getUpdateTime()).orElse(DateUtil.date(0));
            Date date2 = Optional.ofNullable(list2.get(0).getUpdateTime()).orElse(DateUtil.date(0));
            return -DateUtil.compare(date1, date2);
        }).collect(Collectors.toList());

        List<Map.Entry<String, List<ReverseProxyEntity>>> slice = PageEnhancedUtil.slice(pageNum, pageSize, sortedList);

        // flat map 获取分页获得到的所有api列表
        List<Long> apiIdList = slice.stream().flatMap(entry -> entry.getValue().stream().map(ReverseProxyEntity::getId))
                .collect(Collectors.toList());
        List<Endpoint> endpoints = endpointService.listByProxyIdList(apiIdList);

        // endpoints 根据 api id 分组
        Map<Long, Long> apiMap = endpoints.stream().collect(Collectors.groupingBy(Endpoint::getProxyId, Collectors.counting()));

        // 组装成 api resp
        List<ApiResp> records = new ArrayList<>();
        for (Map.Entry<String, List<ReverseProxyEntity>> entry : slice) {
            String apiName = entry.getKey();
            List<ReverseProxyEntity> list = entry.getValue();
            List<VersionSpecifiedResp> versions = list.stream().map(entity -> {
                VersionSpecifiedResp versionSpecifiedResp = new VersionSpecifiedResp();
                Long apiId = entity.getId();
                versionSpecifiedResp.setId(apiId);
                versionSpecifiedResp.setVersion(entity.getVersion());
                versionSpecifiedResp.setDomain(entity.getDomain());
                versionSpecifiedResp.setState(entity.getState());
                versionSpecifiedResp.setUpdateTime(DateUtil.formatDateTime(entity.getUpdateTime()));
                // 设置 endpoint 的总数
                versionSpecifiedResp.setEndpointTotal(Optional.ofNullable(apiMap.get(apiId)).orElse(0L));
                return versionSpecifiedResp;
            }).collect(Collectors.toList());

            ApiResp apiResp = new ApiResp();
            apiResp.setName(apiName);
            apiResp.setVersions(versions);
            records.add(apiResp);
        }

        return new Page(sortedList.size(), records);
    }

    public void delete(Long id) {
        Assert.notNull(id, "api id 不能为空");
        List<Endpoint> endpoints = endpointService.listByProxyIdList(ListUtil.of(id));
        List<Long> endpointIdList = endpoints.stream().map(Endpoint::getId).collect(Collectors.toList());
        endpointService.deleteByIdList(endpointIdList);
        proxyDao.deleteById(id);
    }

    public void changeState(Long id, Integer state) {
        Assert.notNull(id, "api id 不能为空");
        Assert.isTrue(state != null
                && (state == ProxyConstants.STATE_STOP || state == ProxyConstants.STATE_RUNNING),
                "状态不能为空，且只能是 停止或运行");
        ReverseProxyEntity oldEntity = proxyDao.selectById(id);
        oldEntity.setState(state);
        proxyDao.updateById(oldEntity);
    }

    public List<String> listTags() {
        return proxyDao.listTags();
    }

    private static class VersionComparator implements Comparator<ReverseProxyEntity> {

        @Override
        public int compare(ReverseProxyEntity o1, ReverseProxyEntity o2) {
            Date date1 = Optional.ofNullable(o1.getUpdateTime()).orElse(DateUtil.date(0));
            Date date2 = Optional.ofNullable(o2.getUpdateTime()).orElse(DateUtil.date(0));
            // 倒序就取反
            return - DateUtil.compare(date1, date2);
        }
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
