package com.brandnewdata.mop.poc.bff.service.proxy;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.bff.converter.proxy.ProxyEndpointCallVoConverter;
import com.brandnewdata.mop.poc.bff.vo.proxy.operate.ProxyEndpointCallFilter;
import com.brandnewdata.mop.poc.bff.vo.proxy.operate.ProxyEndpointCallVo;
import com.brandnewdata.mop.poc.bff.vo.proxy.operate.ProxyStatistic;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.constant.ProxyConst;
import com.brandnewdata.mop.poc.proxy.bo.ProxyEndpointFilter;
import com.brandnewdata.mop.poc.proxy.bo.ProxyFilter;
import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointCallDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyAService;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyEndpointAService;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyEndpointCallAService;
import com.brandnewdata.mop.poc.proxy.service.combined.IProxyEndpointCService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProxyOperateBffService {

    private final IProxyEndpointCService proxyEndpointCService;

    private final IProxyEndpointAService proxyEndpointAService;

    private final IProxyAService proxyAtomicService;

    private final IProxyEndpointCallAService proxyEndpointCallService;

    public ProxyOperateBffService(IProxyEndpointCService proxyEndpointCService,
                                  IProxyEndpointAService proxyEndpointAService,
                                  IProxyAService proxyAtomicService,
                                  IProxyEndpointCallAService proxyEndpointCallService) {
        this.proxyEndpointCService = proxyEndpointCService;
        this.proxyEndpointAService = proxyEndpointAService;
        this.proxyAtomicService = proxyAtomicService;
        this.proxyEndpointCallService = proxyEndpointCallService;
    }

    public Page<ProxyEndpointCallVo> page(ProxyEndpointCallFilter filter) {
        Integer pageNum = filter.getPageNum();
        Integer pageSize = filter.getPageSize();

        String proxyName = filter.getProxyName();
        String version = filter.getVersion();
        String location = filter.getLocation();

        // 查询proxy
        ProxyFilter proxyFilter = new ProxyFilter().setName(proxyName).setVersion(version);
        List<ProxyDto> proxyDtoList = proxyAtomicService.fetchListByFilter(proxyFilter);
        List<Long> proxyIdList = proxyDtoList.stream().map(ProxyDto::getId).collect(Collectors.toList());

        // 查询 proxyEndpoint
        ProxyEndpointFilter proxyEndpointFilter = new ProxyEndpointFilter().setLocation(location);
        Map<Long, List<ProxyEndpointDto>> proxyEndpointDtoListMap =
                proxyEndpointAService.fetchListByProxyIdAndFilter(proxyIdList, proxyEndpointFilter);
        List<Long> endpointIdList = proxyEndpointDtoListMap.values().stream().flatMap(List::stream)
                .map(ProxyEndpointDto::getId).collect(Collectors.toList());

        Page<ProxyEndpointCallDto> page = proxyEndpointCallService
                .fetchPageByEndpointId(pageNum, pageSize, endpointIdList);

        List<ProxyEndpointCallVo> voList = page.getRecords().stream()
                .map(ProxyEndpointCallVoConverter::createFrom).collect(Collectors.toList());

        return new Page<>(page.getTotal(), voList);
    }

    public ProxyStatistic statistic(ProxyEndpointCallFilter filter) {
        String proxyName = filter.getProxyName();
        String version = filter.getVersion();
        String location = filter.getLocation();

        // 查询proxy
        ProxyFilter proxyFilter = new ProxyFilter().setName(proxyName).setVersion(version);
        List<ProxyDto> proxyDtoList = proxyAtomicService.fetchListByFilter(proxyFilter);
        Map<Long, ProxyDto> proxyDtoMap = proxyDtoList.stream().collect(Collectors.toMap(ProxyDto::getId, Function.identity()));

        // 查询 proxyEndpoint
        ProxyEndpointFilter proxyEndpointFilter = new ProxyEndpointFilter().setLocation(location);
        Map<Long, List<ProxyEndpointDto>> proxyEndpointDtoListMap =
                proxyEndpointAService.fetchListByProxyIdAndFilter(ListUtil.toList(proxyDtoMap.keySet()), proxyEndpointFilter);
        Map<Long, ProxyEndpointDto> proxyEndpointDtoMap = proxyEndpointDtoListMap.values().stream().flatMap(List::stream)
                .collect(Collectors.toMap(ProxyEndpointDto::getId, Function.identity()));

        ProxyStatistic statistic = new ProxyStatistic();

        Map<Long, List<ProxyEndpointCallDto>> proxyEndpointCallDtoListMap =
                proxyEndpointCallService.fetchListByEndpointId(ListUtil.toList(proxyEndpointDtoMap.keySet()));

        AtomicInteger totalCount = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();
        AtomicInteger totalTimeConsuming = new AtomicInteger();
        Map<String, Integer> callCountProxyRankingMap = new HashMap<>();
        Map<String, Integer> timeConsumingProxyRankingMap = new HashMap<>();
        Map<ProxyEndpointDto, Integer> callCountEndpointRankingMap = new HashMap<>();
        Map<ProxyEndpointDto, Integer> timeConsumingEndpointRankingMap = new HashMap<>();
        Map<LocalDate, Integer> callCountTendencyMap = new HashMap<>();
        Map<LocalDate, Integer> averageTimeConsumingTendencyMap = new HashMap<>();


        proxyEndpointCallDtoListMap.values().stream().flatMap(List::stream).forEach(callDto -> {
            totalCount.getAndIncrement();
            if (StrUtil.equals(ProxyConst.CALL_EXECUTE_STATUS__SUCCESS, callDto.getExecuteStatus())) {
                successCount.getAndIncrement();
            } else {
                failCount.getAndIncrement();
            }
            totalTimeConsuming.addAndGet(callDto.getTimeConsuming());

            ProxyDto proxyDto = proxyDtoMap.get(callDto.getProxyId());

            String name = proxyDto.getName();
            if(StrUtil.isNotBlank(name)) {
                callCountProxyRankingMap.put(name,
                        callCountProxyRankingMap.getOrDefault(name, 0) + 1);
                timeConsumingProxyRankingMap.put(name,
                        timeConsumingProxyRankingMap.getOrDefault(name, 0) + callDto.getTimeConsuming());
            }

            ProxyEndpointDto proxyEndpointDto = proxyEndpointDtoMap.get(callDto.getEndpointId());
            if(proxyEndpointDto != null) {
                callCountEndpointRankingMap.put(proxyEndpointDto,
                        callCountEndpointRankingMap.getOrDefault(proxyEndpointDto, 0) + 1);
                timeConsumingEndpointRankingMap.put(proxyEndpointDto,
                        timeConsumingEndpointRankingMap.getOrDefault(proxyEndpointDto, 0) + callDto.getTimeConsuming());
            }

            LocalDateTime startTime = callDto.getStartTime();
            if(startTime != null) {
                LocalDate date = startTime.toLocalDate();
                callCountTendencyMap.put(date, callCountTendencyMap.getOrDefault(date, 0) + 1);
                averageTimeConsumingTendencyMap.put(date,
                        averageTimeConsumingTendencyMap.getOrDefault(date, 0) + callDto.getTimeConsuming());
            }
        });

        // callCountProxyRankingMap.entrySet()


        return statistic;
    }

}
