package com.brandnewdata.mop.poc.proxy.service.atomic;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.proxy.bo.ProxyEndpointServerBo;
import com.brandnewdata.mop.poc.proxy.converter.ProxyEndpointDtoConverter;
import com.brandnewdata.mop.poc.proxy.dao.ProxyEndpointDao;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto;
import com.brandnewdata.mop.poc.proxy.dto.filter.ProxyEndpointFilter;
import com.brandnewdata.mop.poc.proxy.po.ProxyEndpointPo;
import com.dxy.library.json.jackson.JacksonUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProxyEndpoingAService implements IProxyEndpointAService {

    @Resource
    private ProxyEndpointDao proxyEndpointDao;

    @Override
    public Page<ProxyEndpointDto> pageByProxyId(Integer pageNum, Integer pageSize, Long proxyId) {
        Assert.isTrue(pageNum > 0);
        Assert.isTrue(pageSize > 0);
        Assert.notNull(proxyId);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ProxyEndpointPo> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        QueryWrapper<ProxyEndpointPo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ProxyEndpointPo.PROXY_ID, proxyId);
        queryWrapper.isNull(ProxyEndpointPo.DELETE_FLAG);
        page = proxyEndpointDao.selectPage(page, queryWrapper);
        List<ProxyEndpointDto> records = Optional.ofNullable(page.getRecords()).orElse(ListUtil.empty())
                .stream().map(ProxyEndpointDtoConverter::createFrom).collect(Collectors.toList());
        return new Page<>(page.getTotal(), records);
    }

    @Override
    public ProxyEndpointDto fetchByProxyIdAndLocation(Long proxyId, String location) {
        // proxy id not null
        Assert.notNull(proxyId, "proxy id not null");
        Assert.notNull(location, "location not null");
        QueryWrapper<ProxyEndpointPo> query = new QueryWrapper<>();
        query.eq(ProxyEndpointPo.PROXY_ID, proxyId);
        query.eq(ProxyEndpointPo.LOCATION, location);
        query.isNull(ProxyEndpointPo.DELETE_FLAG);

        ProxyEndpointPo proxyEndpointPo = proxyEndpointDao.selectOne(query);
        return proxyEndpointPo == null ? null : ProxyEndpointDtoConverter.createFrom(proxyEndpointPo);
    }

    @Override
    public Map<Long, ProxyEndpointDto> fetchByIds(List<Long> idList) {
        if(CollUtil.isEmpty(idList)) return MapUtil.empty();
        Assert.isFalse(CollUtil.hasNull(idList), "idList can not contain null");

        QueryWrapper<ProxyEndpointPo> query = new QueryWrapper<>();
        query.in(ProxyEndpointPo.ID, idList);
        query.isNull(ProxyEndpointPo.DELETE_FLAG);
        List<ProxyEndpointPo> proxyEndpointPos = proxyEndpointDao.selectList(query);

        return proxyEndpointPos.stream().map(ProxyEndpointDtoConverter::createFrom)
                .collect(Collectors.toMap(com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto::getId, Function.identity()));
    }

    @Override
    public Map<Long, List<ProxyEndpointDto>> fetchListByProxyIdAndFilter(List<Long> proxyIdList, ProxyEndpointFilter filter) {
        if(CollUtil.isEmpty(proxyIdList)) return MapUtil.empty();
        Assert.isFalse(CollUtil.hasNull(proxyIdList), "proxyIdList can not contain null");

        String location = filter.getLocation();

        QueryWrapper<ProxyEndpointPo> query = new QueryWrapper<>();
        query.in(ProxyEndpointPo.PROXY_ID, proxyIdList);
        query.isNull(ProxyEndpointPo.DELETE_FLAG);
        if(StrUtil.isNotBlank(location)) {
            query.eq(ProxyEndpointPo.LOCATION, location);
        }


        List<ProxyEndpointPo> list = proxyEndpointDao.selectList(query);
        return list.stream().map(ProxyEndpointDtoConverter::createFrom)
                .collect(Collectors.groupingBy(ProxyEndpointDto::getProxyId));
    }

    @Override
    public ProxyEndpointServerBo parseServerConfig(String config) {
        ProxyEndpointServerBo bo = JacksonUtil.from(config, ProxyEndpointServerBo.class);
        Assert.notNull(bo.getBaseUrl(), "服务地址不能为空");
        return bo;
    }

    @Override
    public void deleteByProxyId(Long proxyId) {
        Assert.notNull(proxyId, "proxyId must not null");
        UpdateWrapper<ProxyEndpointPo> update = new UpdateWrapper<>();
        update.setSql(StrUtil.format("{} = {}", ProxyEndpointPo.DELETE_FLAG, ProxyEndpointPo.ID));
        update.eq(ProxyEndpointPo.PROXY_ID, proxyId);
        proxyEndpointDao.update(null, update);
    }

    @Override
    public void deleteById(Long id) {
        Assert.notNull(id, "id must not null");
        UpdateWrapper<ProxyEndpointPo> update = new UpdateWrapper<>();
        update.setSql(StrUtil.format("{} = {}", ProxyEndpointPo.DELETE_FLAG, ProxyEndpointPo.ID));
        update.eq(ProxyEndpointPo.ID, id);
        proxyEndpointDao.update(null, update);
    }

    @Override
    public List<String> listTag(Long proxyId) {
        List<String> ret = new ArrayList<>();
        Assert.notNull(proxyId);
        QueryWrapper<ProxyEndpointPo> query = new QueryWrapper<>();
        query.isNull(ProxyEndpointPo.DELETE_FLAG);
        query.isNotNull(ProxyEndpointPo.TAG);
        query.ne(ProxyEndpointPo.TAG, StringPool.EMPTY);
        query.eq(ProxyEndpointPo.PROXY_ID, proxyId);
        query.select(StrUtil.format("distinct {} as {}", ProxyEndpointPo.TAG, ProxyEndpointPo.TAG));
        List<Map<String, Object>> result = proxyEndpointDao.selectMaps(query);
        for (Map<String, Object> map : result) {
            String tag = (String) map.get(ProxyEndpointPo.TAG);
            ret.add(tag);
        }
        return ret;
    }

    @Override
    public Map<Long, Integer> countByProxyId(List<Long> proxyIdList) {
        if(CollUtil.isEmpty(proxyIdList)) return MapUtil.empty();
        Map<Long, Integer> ret = new HashMap<>();
        QueryWrapper<ProxyEndpointPo> query = new QueryWrapper<>();
        query.isNull(ProxyEndpointPo.DELETE_FLAG);
        query.in(ProxyEndpointPo.PROXY_ID, proxyIdList);
        query.select(ProxyEndpointPo.PROXY_ID, "count(*) as num");
        query.groupBy(ProxyEndpointPo.PROXY_ID);
        List<Map<String, Object>> records = proxyEndpointDao.selectMaps(query);

        for (Map<String, Object> record : records) {
            Long proxyId = (Long) record.get(ProxyEndpointPo.PROXY_ID);
            Long num = (Long) record.get("num");
            ret.put(proxyId, num.intValue());
        }

        return ret;
    }
}
