package com.brandnewdata.mop.poc.bff.service.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.NumberUtil;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        Map<String, Integer> callCountProxyRankingSuccessMap = new HashMap<>();
        Map<String, Integer> callCountProxyRankingFalseMap = new HashMap<>();
        Map<String, Double> timeConsumingProxyRankingMap = new HashMap<>();
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
                if(StrUtil.equals(ProxyConst.CALL_EXECUTE_STATUS__SUCCESS, callDto.getExecuteStatus())) {
                    callCountProxyRankingSuccessMap.put(name,
                            callCountProxyRankingSuccessMap.getOrDefault(name, 0) + 1);
                } else {
                    callCountProxyRankingFalseMap.put(name,
                            callCountProxyRankingFalseMap.getOrDefault(name, 0) + 1);
                }

                timeConsumingProxyRankingMap.put(name,
                        timeConsumingProxyRankingMap.getOrDefault(name, 0.0) + callDto.getTimeConsuming());
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

        /*

        List<Pair<String, Integer>> callCountProxyRankingList = callCountProxyRankingMap.entrySet().stream()
                .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                .sorted((o1, o2) -> NumberUtil.compare(o2.getValue(), o1.getValue()))
                .collect(Collectors.toList());

        List<Pair<String, Double>> timeConsumingProxyRankingList = timeConsumingProxyRankingMap.entrySet().stream()
                .map(entry -> {
                    String name = entry.getKey();
                    Double _totalTime = entry.getValue();
                    Integer _totalCount = callCountProxyRankingMap.get(name);
                    BigDecimal averageTime1 = NumberUtil.div(_totalTime, _totalCount, 2);
                    return Pair.of(name, averageTime1.doubleValue());
                })
                .sorted((o1, o2) -> NumberUtil.compare(o2.getValue(), o1.getValue())).collect(Collectors.toList());
*/

        return statistic;
    }

}
