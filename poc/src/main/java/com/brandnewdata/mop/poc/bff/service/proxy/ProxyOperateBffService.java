package com.brandnewdata.mop.poc.bff.service.proxy;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.bff.converter.proxy.ProxyEndpointCallVoConverter;
import com.brandnewdata.mop.poc.bff.converter.proxy.ProxyEndpointDtoConverter;
import com.brandnewdata.mop.poc.bff.vo.operate.charts.ChartOption;
import com.brandnewdata.mop.poc.bff.vo.operate.charts.Series;
import com.brandnewdata.mop.poc.bff.vo.proxy.operate.ProxyEndpointCallVo;
import com.brandnewdata.mop.poc.bff.vo.proxy.operate.ProxyStatistic;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.constant.ProxyConst;
import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointCallDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto;
import com.brandnewdata.mop.poc.proxy.dto.filter.ProxyEndpointCallFilter;
import com.brandnewdata.mop.poc.proxy.dto.filter.ProxyEndpointFilter;
import com.brandnewdata.mop.poc.proxy.dto.filter.ProxyFilter;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyAService;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyEndpointAService;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyEndpointCallAService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProxyOperateBffService {

    private final IProxyEndpointAService proxyEndpointAService;

    private final IProxyAService proxyAtomicService;

    private final IProxyEndpointCallAService proxyEndpointCallService;

    private final int MAX_SIZE = 5;

    public ProxyOperateBffService(IProxyEndpointAService proxyEndpointAService,
                                  IProxyAService proxyAtomicService,
                                  IProxyEndpointCallAService proxyEndpointCallService) {
        this.proxyEndpointAService = proxyEndpointAService;
        this.proxyAtomicService = proxyAtomicService;
        this.proxyEndpointCallService = proxyEndpointCallService;
    }

    public Page<ProxyEndpointCallVo> page(com.brandnewdata.mop.poc.bff.vo.proxy.operate.ProxyEndpointCallFilter filter) {
        Integer pageNum = filter.getPageNum();
        Integer pageSize = filter.getPageSize();

        String proxyName = filter.getProxyName();
        String version = filter.getVersion();
        String location = filter.getLocation();
        Long projectId = Opt.ofNullable(filter.getProjectId()).map(Long::valueOf).orElse(null);
        LocalDateTime minStartTime = Opt.ofNullable(filter.getStartTime()).map(date -> DateUtil.parse(date).toLocalDateTime()).orElse(null);
        LocalDateTime maxStartTime = Opt.ofNullable(filter.getEndTime()).map(date -> DateUtil.parse(date).toLocalDateTime()).orElse(null);

        // 查询proxy
        ProxyFilter proxyFilter = new ProxyFilter().setName(proxyName).setVersion(version).setProjectId(projectId);
        List<ProxyDto> proxyDtoList = proxyAtomicService.fetchCacheListByFilter(proxyFilter);
        Map<Long, ProxyDto> proxyDtoMap = proxyDtoList.stream().collect(Collectors.toMap(ProxyDto::getId, Function.identity()));

        // 查询 proxyEndpoint
        ProxyEndpointFilter proxyEndpointFilter = new ProxyEndpointFilter().setLocation(location);
        Map<Long, List<ProxyEndpointDto>> proxyEndpointDtoListMap =
                proxyEndpointAService.fetchListByProxyIdAndFilter(ListUtil.toList(proxyDtoMap.keySet()), proxyEndpointFilter);
        Map<Long, ProxyEndpointDto> proxyEndpointDtoMap = proxyEndpointDtoListMap.values().stream().flatMap(List::stream)
                .collect(Collectors.toMap(ProxyEndpointDto::getId, Function.identity()));

        // 更新 proxy endpoint 信息
        for (ProxyEndpointDto proxyEndpointDto : proxyEndpointDtoMap.values()) {
            ProxyDto proxyDto = proxyDtoMap.get(proxyEndpointDto.getProxyId());
            ProxyEndpointDtoConverter.updateFrom(proxyEndpointDto, proxyDto);
        }

        ProxyEndpointCallFilter proxyEndpointCallFilter = new ProxyEndpointCallFilter()
                .setMinStartTime(minStartTime).setMaxStartTime(maxStartTime);
        Page<ProxyEndpointCallDto> page = proxyEndpointCallService
                .fetchPageByEndpointId(pageNum, pageSize, ListUtil.toList(proxyEndpointDtoMap.keySet()), proxyEndpointCallFilter);

        List<ProxyEndpointCallVo> voList = page.getRecords().stream()
                .map(callDto -> ProxyEndpointCallVoConverter.createFrom(callDto, proxyEndpointDtoMap.get(callDto.getEndpointId())))
                .collect(Collectors.toList());

        Page<ProxyEndpointCallVo> ret = new Page<>(page.getTotal(), voList);
        ret.setExtraMap(page.getExtraMap());
        return ret;
    }

    public ProxyStatistic statistic(com.brandnewdata.mop.poc.bff.vo.proxy.operate.ProxyEndpointCallFilter filter) {
        ProxyStatistic statistic = new ProxyStatistic();
        String proxyName = filter.getProxyName();
        String version = filter.getVersion();
        String location = filter.getLocation();
        Long projectId = Opt.ofNullable(filter.getProjectId()).map(Long::valueOf).orElse(null);
        LocalDateTime minStartTime = Opt.ofNullable(filter.getStartTime()).map(date -> DateUtil.parse(date).toLocalDateTime()).orElse(null);
        LocalDateTime maxStartTime = Opt.ofNullable(filter.getEndTime()).map(date -> DateUtil.parse(date).toLocalDateTime()).orElse(null);

        // 查询proxy
        ProxyFilter proxyFilter = new ProxyFilter().setName(proxyName).setVersion(version).setProjectId(projectId);
        List<ProxyDto> proxyDtoList = proxyAtomicService.fetchCacheListByFilter(proxyFilter);
        Map<Long, ProxyDto> proxyDtoMap = proxyDtoList.stream().collect(Collectors.toMap(ProxyDto::getId, Function.identity()));

        // 查询 proxyEndpoint
        ProxyEndpointFilter proxyEndpointFilter = new ProxyEndpointFilter().setLocation(location);
        Map<Long, List<ProxyEndpointDto>> proxyEndpointDtoListMap =
                proxyEndpointAService.fetchListByProxyIdAndFilter(ListUtil.toList(proxyDtoMap.keySet()), proxyEndpointFilter);
        Map<Long, ProxyEndpointDto> proxyEndpointDtoMap = proxyEndpointDtoListMap.values().stream().flatMap(List::stream)
                .collect(Collectors.toMap(ProxyEndpointDto::getId, Function.identity()));

        // 更新 proxy endpoint 信息
        for (ProxyEndpointDto proxyEndpointDto : proxyEndpointDtoMap.values()) {
            ProxyDto proxyDto = proxyDtoMap.get(proxyEndpointDto.getProxyId());
            ProxyEndpointDtoConverter.updateFrom(proxyEndpointDto, proxyDto);
        }

        // 查询所有api调用明细
        ProxyEndpointCallFilter proxyEndpointCallFilter = new ProxyEndpointCallFilter()
                .setMinStartTime(minStartTime).setMaxStartTime(maxStartTime);
        Map<Long, List<ProxyEndpointCallDto>> proxyEndpointCallDtoListMap =
                proxyEndpointCallService.fetchCacheListByEndpointId(ListUtil.toList(proxyEndpointDtoMap.keySet()), proxyEndpointCallFilter);

        AtomicInteger totalCount = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger falseCount = new AtomicInteger();
        AtomicInteger totalTimeConsuming = new AtomicInteger();
        Map<String, Integer> callCountProxyRankingMap = new HashMap<>();
        Map<String, Integer> callCountProxyRankingSuccessMap = new HashMap<>();
        Map<String, Integer> callCountProxyRankingFalseMap = new HashMap<>();
        Map<String, Double> timeConsumingProxyRankingMap = new HashMap<>();
        Map<ProxyEndpointDto, Integer> callCountEndpointRankingMap = new HashMap<>();
        Map<ProxyEndpointDto, Integer> callCountEndpointRankingSuccessMap = new HashMap<>();
        Map<ProxyEndpointDto, Integer> callCountEndpointRankingFalseMap = new HashMap<>();
        Map<ProxyEndpointDto, Double> timeConsumingEndpointRankingMap = new HashMap<>();
        Map<LocalDate, Integer> callCountTendencyMap = new HashMap<>();
        Map<LocalDate, Integer> callCountTendencySuccessMap = new HashMap<>();
        Map<LocalDate, Integer> callCountTendencyFalseMap = new HashMap<>();
        Map<LocalDate, Double> timeConsumingTendencyMap = new HashMap<>();

        proxyEndpointCallDtoListMap.values().stream().flatMap(List::stream).forEach(callDto -> {
            totalCount.getAndIncrement();
            if (StrUtil.equals(ProxyConst.CALL_EXECUTE_STATUS__SUCCESS, callDto.getExecuteStatus())) {
                successCount.getAndIncrement();
            } else {
                falseCount.getAndIncrement();
            }
            totalTimeConsuming.addAndGet(callDto.getTimeConsuming());
            ProxyEndpointDto proxyEndpointDto = proxyEndpointDtoMap.get(callDto.getEndpointId());
            if(proxyEndpointDto == null) return;
            ProxyDto proxyDto = proxyDtoMap.get(proxyEndpointDto.getProxyId());
            if(proxyDto == null) return;

            String name = proxyDto.getName();
            if(StrUtil.isNotBlank(name)) {
                callCountProxyRankingMap.put(name, callCountProxyRankingMap.getOrDefault(name, 0) + 1);
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

            callCountEndpointRankingMap.put(proxyEndpointDto,
                    callCountEndpointRankingMap.getOrDefault(proxyEndpointDto, 0) + 1);
            if (StrUtil.equals(ProxyConst.CALL_EXECUTE_STATUS__SUCCESS, callDto.getExecuteStatus())) {
                callCountEndpointRankingSuccessMap.put(proxyEndpointDto,
                        callCountEndpointRankingSuccessMap.getOrDefault(proxyEndpointDto, 0) + 1);
            } else {
                callCountEndpointRankingFalseMap.put(proxyEndpointDto,
                        callCountEndpointRankingFalseMap.getOrDefault(proxyEndpointDto, 0) + 1);
            }
            timeConsumingEndpointRankingMap.put(proxyEndpointDto,
                    timeConsumingEndpointRankingMap.getOrDefault(proxyEndpointDto, 0.0) + callDto.getTimeConsuming());

            LocalDateTime startTime = callDto.getStartTime();
            if(startTime != null) {
                LocalDate date = startTime.toLocalDate();
                callCountTendencyMap.put(date, callCountTendencyMap.getOrDefault(date, 0) + 1);
                if(StrUtil.equals(ProxyConst.CALL_EXECUTE_STATUS__SUCCESS, callDto.getExecuteStatus())) {
                    callCountTendencySuccessMap.put(date, callCountTendencySuccessMap.getOrDefault(date, 0) + 1);
                } else {
                    callCountTendencyFalseMap.put(date, callCountTendencyFalseMap.getOrDefault(date, 0) + 1);
                }
                timeConsumingTendencyMap.put(date,
                        timeConsumingTendencyMap.getOrDefault(date, 0.0) + callDto.getTimeConsuming());
            }
        });

        // 组装数据
        assembleCount(statistic, totalCount.get(), successCount.get(), falseCount.get(), totalTimeConsuming.get());
        assembleCallCountProxyRanking(statistic, callCountProxyRankingMap, callCountProxyRankingSuccessMap, callCountProxyRankingFalseMap);
        assembleTimeConsumingProxyRanking(statistic, timeConsumingProxyRankingMap, callCountProxyRankingMap);
        assembleCallCountEndpointRanking(statistic, callCountEndpointRankingMap, callCountEndpointRankingSuccessMap, callCountEndpointRankingFalseMap);
        assembleTimeConsumingEndpointRanking(statistic, timeConsumingEndpointRankingMap, callCountEndpointRankingMap);
        assembleCallCountTendency(statistic, callCountTendencyMap, callCountTendencySuccessMap, callCountTendencyFalseMap);
        assembleTimeConsumingTendency(statistic, timeConsumingTendencyMap, callCountTendencyMap);
        return statistic;
    }

    private void assembleCount(ProxyStatistic statistic, int totalCount, int successCount, int failCount, int totalTimeConsuming) {
        statistic.setTotalCount(totalCount);
        statistic.setSuccessCount(successCount);
        statistic.setFailCount(failCount);
        if(totalCount == 0) {
            statistic.setAverageTimeConsuming(0.0);
        } else {
            statistic.setAverageTimeConsuming(NumberUtil.div(totalTimeConsuming * 1.0, totalCount, 2));
        }

    }

    private void assembleCallCountProxyRanking(ProxyStatistic statistic,
                                               Map<String, Integer> callCountProxyRankingMap,
                                               Map<String, Integer> callCountProxyRankingSuccessMap,
                                               Map<String, Integer> callCountProxyRankingFalseMap) {
        ChartOption chart = new ChartOption();
        List<Pair<String, Integer>> callCountProxyRankingList = callCountProxyRankingMap.entrySet().stream()
                .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                .sorted((o1, o2) -> NumberUtil.compare(o2.getValue(), o1.getValue()))
                .collect(Collectors.toList());
        List<String> categoryList = new ArrayList<>();
        List<Integer> successDataList = new ArrayList<>();
        List<Integer> falseDataList = new ArrayList<>();

        for (int i = 0; i < callCountProxyRankingList.size() && i < MAX_SIZE; i++) {
            Pair<String, Integer> pair = callCountProxyRankingList.get(i);
            String name = pair.getKey();
            Integer successCount = callCountProxyRankingSuccessMap.getOrDefault(name, 0);
            Integer falseCount = callCountProxyRankingFalseMap.getOrDefault(name, 0);
            categoryList.add(name);
            successDataList.add(successCount);
            falseDataList.add(falseCount);
        }

        chart.setCategory(categoryList.toArray());
        Series seriesSuccess = new Series("请求成功次数", successDataList.toArray());
        Series seriesFalse = new Series("请求失败次数", falseDataList.toArray());
        chart.setSeries(new Series[]{seriesSuccess, seriesFalse});

        statistic.setCallCountProxyRanking(chart);
    }

    private void assembleTimeConsumingProxyRanking(ProxyStatistic statistic,
                                                   Map<String, Double> timeConsumingProxyRankingMap,
                                                   Map<String, Integer> callCountProxyRankingMap) {
        ChartOption chart = new ChartOption();
        List<Pair<String, Double>> timeConsumingProxyRankingList = timeConsumingProxyRankingMap.entrySet().stream()
                .map(entry -> {
                    String name = entry.getKey();
                    Double _totalTime = entry.getValue();
                    Integer _totalCount = callCountProxyRankingMap.get(name);
                    BigDecimal averageTime1 = NumberUtil.div(_totalTime, _totalCount, 2);
                    return Pair.of(name, averageTime1.doubleValue());
                })
                .sorted((o1, o2) -> NumberUtil.compare(o2.getValue(), o1.getValue())).collect(Collectors.toList());

        List<String> categoryList = new ArrayList<>();
        List<Double> dataList = new ArrayList<>();

        for (int i = 0; i < timeConsumingProxyRankingList.size() && i < MAX_SIZE; i++) {
            Pair<String, Double> pair = timeConsumingProxyRankingList.get(i);
            categoryList.add(pair.getKey());
            dataList.add(pair.getValue());
        }

        chart.setCategory(categoryList.toArray());
        Series series = new Series("平均请求耗时", dataList.toArray());
        chart.setSeries(new Series[]{series});

        statistic.setTimeConsumingProxyRanking(chart);
    }

    private void assembleCallCountEndpointRanking(ProxyStatistic statistic,
                                                  Map<ProxyEndpointDto, Integer> callCountEndpointRankingMap,
                                                  Map<ProxyEndpointDto, Integer> callCountEndpointRankingSuccessMap,
                                                  Map<ProxyEndpointDto, Integer> callCountEndpointRankingFalseMap) {
        ChartOption chart = new ChartOption();
        List<Pair<ProxyEndpointDto, Integer>> callCountProxyRankingList = callCountEndpointRankingMap.entrySet().stream()
                .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                .sorted((o1, o2) -> NumberUtil.compare(o2.getValue(), o1.getValue()))
                .collect(Collectors.toList());
        List<String> categoryList = new ArrayList<>();
        List<Integer> successDataList = new ArrayList<>();
        List<Integer> falseDataList = new ArrayList<>();

        for (int i = 0; i < callCountProxyRankingList.size() && i < MAX_SIZE; i++) {
            Pair<ProxyEndpointDto, Integer> pair = callCountProxyRankingList.get(i);
            ProxyEndpointDto endpointDto = pair.getKey();
            Integer successCount = callCountEndpointRankingSuccessMap.getOrDefault(endpointDto, 0);
            Integer falseCount = callCountEndpointRankingFalseMap.getOrDefault(endpointDto, 0);
            categoryList.add(StrUtil.format("{}\n{}({})", endpointDto.getLocation(),
                    endpointDto.getProxyName(), endpointDto.getProxyVersion()));
            successDataList.add(successCount);
            falseDataList.add(falseCount);
        }

        chart.setCategory(categoryList.toArray());
        Series seriesSuccess = new Series("请求成功次数", successDataList.toArray());
        Series seriesFalse = new Series("请求失败次数", falseDataList.toArray());
        chart.setSeries(new Series[]{seriesSuccess, seriesFalse});

        statistic.setCallCountEndpointRanking(chart);
    }

    private void assembleTimeConsumingEndpointRanking(ProxyStatistic statistic,
                                                      Map<ProxyEndpointDto, Double> timeConsumingEndpointRankingMap,
                                                      Map<ProxyEndpointDto, Integer> callCountEndpointRankingMap) {
        ChartOption chart = new ChartOption();
        List<Pair<ProxyEndpointDto, Double>> timeConsumingEndpointRankingList = timeConsumingEndpointRankingMap.entrySet().stream()
                .map(entry -> {
                    ProxyEndpointDto endpointDto = entry.getKey();
                    Double _totalTime = entry.getValue();
                    Integer _totalCount = callCountEndpointRankingMap.get(endpointDto);
                    BigDecimal averageTime1 = NumberUtil.div(_totalTime, _totalCount, 2);
                    return Pair.of(endpointDto, averageTime1.doubleValue());
                })
                .sorted((o1, o2) -> NumberUtil.compare(o2.getValue(), o1.getValue())).collect(Collectors.toList());

        List<String> categoryList = new ArrayList<>();
        List<Double> dataList = new ArrayList<>();
        for (int i = 0; i < timeConsumingEndpointRankingList.size() && i < MAX_SIZE; i++) {
            Pair<ProxyEndpointDto, Double> pair = timeConsumingEndpointRankingList.get(i);
            ProxyEndpointDto endpointDto = pair.getKey();
            categoryList.add(StrUtil.format("{}\n{}({})", endpointDto.getLocation(),
                    endpointDto.getProxyName(), endpointDto.getProxyVersion()));
            dataList.add(pair.getValue());
        }

        chart.setCategory(categoryList.toArray());
        Series series = new Series("平均请求耗时", dataList.toArray());
        chart.setSeries(new Series[]{series});
        statistic.setTimeConsumingEndpointRanking(chart);
    }

    private void assembleCallCountTendency(ProxyStatistic statistic,
                                           Map<LocalDate, Integer> callCountTendencyMap,
                                           Map<LocalDate, Integer> callCountTendencySuccessMap,
                                           Map<LocalDate, Integer> callCountTendencyFalseMap) {
        ChartOption chart = new ChartOption();
        List<Pair<LocalDate, Integer>> callCountTendencyList = callCountTendencyMap.entrySet().stream()
                .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                .sorted((o1, o2) -> o2.getKey().compareTo(o1.getKey())).collect(Collectors.toList());

        List<String> categoryList = new ArrayList<>();
        List<Integer> successDataList = new ArrayList<>();
        List<Integer> falseDataList = new ArrayList<>();
        for (int i = 0; i < callCountTendencyList.size(); i++) {
            Pair<LocalDate, Integer> pair = callCountTendencyList.get(i);
            LocalDate date = pair.getKey();
            categoryList.add(LocalDateTimeUtil.formatNormal(date));
            Integer successCount = callCountTendencySuccessMap.getOrDefault(date, 0);
            Integer falseCount = callCountTendencyFalseMap.getOrDefault(date, 0);
            successDataList.add(successCount);
            falseDataList.add(falseCount);
        }

        chart.setCategory(categoryList.toArray());
        Series seriesSuccess = new Series("请求成功次数", successDataList.toArray());
        Series seriesFalse = new Series("请求失败次数", falseDataList.toArray());
        chart.setSeries(new Series[]{seriesSuccess, seriesFalse});

        statistic.setCallCountTendency(chart);
    }

    private void assembleTimeConsumingTendency(ProxyStatistic statistic,
                                                  Map<LocalDate, Double> timeConsumingTendencyMap,
                                                  Map<LocalDate, Integer> callCountTendencyMap) {
        ChartOption chart = new ChartOption();
        List<Pair<LocalDate, Double>> timeConsumingTendencyList = timeConsumingTendencyMap.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    Double _totalTime = entry.getValue();
                    Integer _totalCount = callCountTendencyMap.get(date);
                    BigDecimal averageTime1 = NumberUtil.div(_totalTime, _totalCount, 2);
                    return Pair.of(date, averageTime1.doubleValue());
                })
                .sorted((o1, o2) -> o2.getKey().compareTo(o1.getKey())).collect(Collectors.toList());

        List<String> categoryList = new ArrayList<>();
        List<Double> dataList = new ArrayList<>();
        for (int i = 0; i < timeConsumingTendencyList.size(); i++) {
            Pair<LocalDate, Double> pair = timeConsumingTendencyList.get(i);
            LocalDate date = pair.getKey();
            categoryList.add(LocalDateTimeUtil.formatNormal(date));
            dataList.add(pair.getValue());
        }

        chart.setCategory(categoryList.toArray());
        Series series = new Series("平均请求耗时", dataList.toArray());
        chart.setSeries(new Series[]{series});
        statistic.setTimeConsumingTendency(chart);
    }
}
