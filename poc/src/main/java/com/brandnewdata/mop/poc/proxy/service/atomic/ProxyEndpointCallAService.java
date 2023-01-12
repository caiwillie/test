package com.brandnewdata.mop.poc.proxy.service.atomic;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.constant.ProxyConst;
import com.brandnewdata.mop.poc.proxy.cache.ProxyEndpointCallCache;
import com.brandnewdata.mop.poc.proxy.converter.ProxyEndpointCallDtoConverter;
import com.brandnewdata.mop.poc.proxy.converter.ProxyEndpointCallPoConverter;
import com.brandnewdata.mop.poc.proxy.dao.ProxyEndpointCallDao;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointCallDto;
import com.brandnewdata.mop.poc.proxy.dto.filter.ProxyEndpointCallFilter;
import com.brandnewdata.mop.poc.proxy.po.ProxyEndpointCallPo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProxyEndpointCallAService implements IProxyEndpointCallAService {

    @Resource
    private ProxyEndpointCallDao proxyEndpointCallDao;

    private final ProxyEndpointCallCache proxyEndpointCallCache;

    public ProxyEndpointCallAService(ProxyEndpointCallCache proxyEndpointCallCache) {
        this.proxyEndpointCallCache = proxyEndpointCallCache;
    }

    @Override
    public Page<ProxyEndpointCallDto> fetchPageByEndpointId(Integer pageNum, Integer pageSize,
                                                            List<Long> endpointIdList, ProxyEndpointCallFilter filter) {
        Assert.isTrue(pageNum > 0, "pageNum must be greater than 0");
        Assert.isTrue(pageSize > 0, "pageSize must be greater than 0");
        if(CollUtil.isEmpty(endpointIdList)) return Page.empty();
        Assert.notNull(filter);
        LocalDateTime minStartTime = filter.getMinStartTime();
        LocalDateTime maxStartTime = filter.getMaxStartTime();

        QueryWrapper<ProxyEndpointCallPo> query = new QueryWrapper<>();
        query.in(ProxyEndpointCallPo.ENDPOINT_ID, endpointIdList);
        if(minStartTime != null) {
            query.ge(ProxyEndpointCallPo.CREATE_TIME, minStartTime);
        }

        if(maxStartTime != null) {
            query.le(ProxyEndpointCallPo.CREATE_TIME, maxStartTime);
        }

        query.orderByDesc(ProxyEndpointCallPo.CREATE_TIME);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ProxyEndpointCallPo> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        page = proxyEndpointCallDao.selectPage(page, query);

        // 统计成功数、失败数
        Map<String, Long> countMap = new HashMap<>();
        QueryWrapper<ProxyEndpointCallPo> query2 = new QueryWrapper<>();
        query2.in(ProxyEndpointCallPo.ENDPOINT_ID, endpointIdList);
        if(minStartTime != null) {
            query2.ge(ProxyEndpointCallPo.CREATE_TIME, minStartTime);
        }
        if(maxStartTime != null) {
            query2.le(ProxyEndpointCallPo.CREATE_TIME, maxStartTime);
        }
        query2.groupBy(ProxyEndpointCallPo.EXECUTE_STATUS);
        query2.select(ProxyEndpointCallPo.EXECUTE_STATUS, "count(*) as num");
        List<Map<String, Object>> countResultList = proxyEndpointCallDao.selectMaps(query2);
        for (Map<String, Object> map : countResultList) {
            String status = (String) map.get(ProxyEndpointCallPo.EXECUTE_STATUS);
            if(status == null) continue;
            // todo caiwillie 魔法值优化
            Long num = (Long) map.get("num");
            if(StrUtil.equals(status, ProxyConst.CALL_EXECUTE_STATUS__SUCCESS)) {
                countMap.put("successCount", num);
            } else {
                countMap.put("failCount", countMap.getOrDefault(ProxyConst.CALL_EXECUTE_STATUS__FAIL, 0L) + num);
            }
        }

        List<ProxyEndpointCallPo> records = page.getRecords();
        List<ProxyEndpointCallDto> dtoList = records.stream()
                .map(ProxyEndpointCallDtoConverter::createFrom).collect(Collectors.toList());
        Page<ProxyEndpointCallDto> ret = new Page<>(page.getTotal(), dtoList);
        ret.setExtraMap(countMap);
        return ret;
    }

    @Override
    public ProxyEndpointCallDto save(ProxyEndpointCallDto dto) {
        dto.setId(IdUtil.getSnowflakeNextId());
        proxyEndpointCallDao.insert(ProxyEndpointCallPoConverter.createFrom(dto));
        return dto;
    }

    @Override
    public Map<Long, List<ProxyEndpointCallDto>> fetchCacheListByEndpointId(List<Long> endpointIdList, ProxyEndpointCallFilter filter) {
        if(CollUtil.isEmpty(endpointIdList)) return MapUtil.empty();
        Assert.isFalse(CollUtil.hasNull(endpointIdList), "endpointIdList must not contain null");

        LocalDateTime maxStartTime = filter.getMaxStartTime();
        LocalDateTime minStartTime = filter.getMinStartTime();

        Map<Long, ProxyEndpointCallDto> proxyEndpointCallDtoMap = proxyEndpointCallCache.asMap();

        Map<Long, List<ProxyEndpointCallDto>> proxyEndpointCallDtoListMap = new HashMap<>();
        for (ProxyEndpointCallDto proxyEndpointCallDto : proxyEndpointCallDtoMap.values()) {
            Long endpointId = proxyEndpointCallDto.getEndpointId();
            if(!endpointIdList.contains(endpointId)) continue;
            LocalDateTime startTime = proxyEndpointCallDto.getStartTime();
            // 最小开始时间， 最大开始时间
            if(minStartTime != null && minStartTime.compareTo(startTime) > 0) continue;
            if(maxStartTime != null && maxStartTime.compareTo(startTime) < 0) continue;

            List<ProxyEndpointCallDto> proxyEndpointCallDtoList =
                    proxyEndpointCallDtoListMap.computeIfAbsent(endpointId, k -> CollUtil.newArrayList());
            proxyEndpointCallDtoList.add(proxyEndpointCallDto);
        }

        return proxyEndpointCallDtoListMap;
    }

}
