package com.brandnewdata.mop.poc.proxy.service.atomic;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
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
import com.brandnewdata.mop.poc.proxy.dto.agg.ProxyEndpointCallAgg;
import com.brandnewdata.mop.poc.proxy.dto.filter.ProxyEndpointCallFilter;
import com.brandnewdata.mop.poc.proxy.po.ProxyEndpointCallPo;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProxyEndpointCallAService implements IProxyEndpointCallAService {

    private final ProxyEndpointCallDao proxyEndpointCallDao;

    private final ProxyEndpointCallCache proxyEndpointCallCache;

    public ProxyEndpointCallAService(ProxyEndpointCallDao proxyEndpointCallDao,
                                     ProxyEndpointCallCache proxyEndpointCallCache) {
        this.proxyEndpointCallDao = proxyEndpointCallDao;
        this.proxyEndpointCallCache = proxyEndpointCallCache;
    }

    @Override
    public Page<ProxyEndpointCallDto> fetchPageByEndpointId(Integer pageNum, Integer pageSize,
                                                            List<Long> endpointIdList, ProxyEndpointCallFilter filter) {
        Assert.isTrue(pageNum > 0, "pageNum must be greater than 0");
        Assert.isTrue(pageSize > 0, "pageSize must be greater than 0");
        if(CollUtil.isEmpty(endpointIdList)) return Page.empty();

        QueryWrapper<ProxyEndpointCallPo> query = assembleQuery(endpointIdList, filter);

        query.orderByDesc(ProxyEndpointCallPo.CREATE_TIME);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ProxyEndpointCallPo> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        page = proxyEndpointCallDao.selectPage(page, query);

        // 统计成功数、失败数
        Map<String, Long> countMap = new HashMap<>();
        QueryWrapper<ProxyEndpointCallPo> query2 = assembleQuery(endpointIdList, filter);
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
                countMap.put("failCount", countMap.getOrDefault("failCount", 0L) + num);
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

    @Override
    public List<ProxyEndpointCallAgg> aggProxyEndpointCallByEndpointId(List<Long> endpointIdList, ProxyEndpointCallFilter filter) {
        List<ProxyEndpointCallAgg> ret = new ArrayList<>();
        if(CollUtil.isEmpty(endpointIdList)) return ListUtil.empty();
        Assert.isFalse(CollUtil.hasNull(endpointIdList), "endpointIdList must not contain null");

        QueryWrapper<ProxyEndpointCallPo> query = assembleQuery(endpointIdList, filter);
        String createDateAgg = StrUtil.format("DATE({})", ProxyEndpointCallPo.CREATE_TIME);
        query.groupBy(ProxyEndpointCallPo.ENDPOINT_ID, createDateAgg, ProxyEndpointCallPo.EXECUTE_STATUS);
        query.select(ProxyEndpointCallPo.ENDPOINT_ID,
                StrUtil.format("{} as create_date", createDateAgg),
                ProxyEndpointCallPo.EXECUTE_STATUS,
                StrUtil.format("COUNT(*) as row_count"),
                StrUtil.format("SUM({}) as time_consume_sum", ProxyEndpointCallPo.TIME_CONSUMING));
        List<Map<String, Object>> records = proxyEndpointCallDao.selectMaps(query);
        if(CollUtil.isEmpty(records)) return ret;

        for (Map<String, Object> record : records) {
            ProxyEndpointCallAgg agg = new ProxyEndpointCallAgg();
            Long endpointId = (Long) record.get(ProxyEndpointCallPo.ENDPOINT_ID);
            LocalDate createDate = Opt.ofNullable((Date) record.get("create_date")).map(Date::toLocalDate).orElse(null);
            String executeStatus = (String) record.get(ProxyEndpointCallPo.EXECUTE_STATUS);
            Long rowCount = (Long) record.get("row_count");
            Long timeConsumeSum = (Long) record.get("time_consume_sum");
            agg.setEndpointId(endpointId);
            agg.setCreateDate(createDate);
            agg.setExecuteStatus(executeStatus);
            agg.setRowCount(rowCount);
            agg.setTimeConsumeSum(timeConsumeSum);
            ret.add(agg);
        }
        return ret;
    }


    private QueryWrapper<ProxyEndpointCallPo> assembleQuery(List<Long> endpointIdList, ProxyEndpointCallFilter filter) {
        Assert.notEmpty(endpointIdList);
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
        return query;
    }

}
