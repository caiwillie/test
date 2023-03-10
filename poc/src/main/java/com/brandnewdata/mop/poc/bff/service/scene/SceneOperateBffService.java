package com.brandnewdata.mop.poc.bff.service.scene;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.bff.converter.process.ProcessDefinitionVoConverter;
import com.brandnewdata.mop.poc.bff.converter.scene.OperateProcessInstanceVoConverter;
import com.brandnewdata.mop.poc.bff.vo.operate.charts.ChartOption;
import com.brandnewdata.mop.poc.bff.vo.operate.charts.Series;
import com.brandnewdata.mop.poc.bff.vo.process.ProcessDefinitionVo;
import com.brandnewdata.mop.poc.bff.vo.scene.operate.OperateProcessInstanceVo;
import com.brandnewdata.mop.poc.bff.vo.scene.operate.SceneDeployFilter;
import com.brandnewdata.mop.poc.bff.vo.scene.operate.SceneStatistic;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDto;
import com.brandnewdata.mop.poc.operate.dto.ProcessInstanceStateDto;
import com.brandnewdata.mop.poc.operate.dto.filter.ProcessInstanceFilter;
import com.brandnewdata.mop.poc.operate.dto.statistic.ProcessInstanceKeyAgg;
import com.brandnewdata.mop.poc.operate.service.IProcessInstanceService;
import com.brandnewdata.mop.poc.process.dto.ProcessReleaseDeployDto;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService;
import com.brandnewdata.mop.poc.scene.dto.SceneDto;
import com.brandnewdata.mop.poc.scene.dto.SceneReleaseDeployDto;
import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;
import com.brandnewdata.mop.poc.scene.service.ISceneService;
import com.brandnewdata.mop.poc.scene.service.atomic.ISceneReleaseDeployAService;
import com.brandnewdata.mop.poc.scene.service.atomic.IVersionProcessAService;
import io.camunda.operate.dto.ProcessInstanceState;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SceneOperateBffService {
    private final int MAX_SIZE = 10;
    private final ISceneReleaseDeployAService sceneReleaseDeployService;

    private final IProcessDeployService processDeployService;

    private final IProcessInstanceService processInstanceService;

    private final IVersionProcessAService versionProcessAService;

    private final ISceneService sceneService;

    public SceneOperateBffService(ISceneReleaseDeployAService sceneReleaseDeployService,
                                  IProcessDeployService processDeployService,
                                  IProcessInstanceService processInstanceService,
                                  IVersionProcessAService versionProcessAService,
                                  ISceneService sceneService) {
        this.sceneReleaseDeployService = sceneReleaseDeployService;
        this.processDeployService = processDeployService;
        this.processInstanceService = processInstanceService;
        this.versionProcessAService = versionProcessAService;
        this.sceneService = sceneService;
    }

    public Page<OperateProcessInstanceVo> pageProcessInstance(SceneDeployFilter filter) {
        Long envId = Assert.notNull(filter.getEnvId());

        LocalDateTime minStartTime = Opt.ofNullable(filter.getStartTime()).map(time -> DateUtil.parse(time).toLocalDateTime()).orElse(LocalDateTime.now().minusDays(1));
        LocalDateTime maxStartTime = Opt.ofNullable(filter.getEndTime()).map(time -> DateUtil.parse(time).toLocalDateTime()).orElse(LocalDateTime.now());

        // ?????????????????????????????????id??????
        List<SceneReleaseDeployDto> sceneReleaseDeployDtoList = fetchSceneReleaseDeployDtoList(filter);
        Map<String, SceneReleaseDeployDto> sceneReleaseDeployDtoMap = sceneReleaseDeployDtoList.stream()
                .collect(Collectors.toMap(SceneReleaseDeployDto::getProcessId, Function.identity()));

        // ?????? process release deploy??????
        Map<String, ProcessReleaseDeployDto> processReleaseDeployDtoMap =
                processDeployService.fetchReleaseByEnvIdAndProcessId(envId, ListUtil.toList(sceneReleaseDeployDtoMap.keySet()));

        List<Long> zeebeKeyList = processReleaseDeployDtoMap.values().stream()
                .map(ProcessReleaseDeployDto::getProcessZeebeKey).collect(Collectors.toList());

        // ??????????????????
        ProcessInstanceFilter processInstanceFilter = new ProcessInstanceFilter()
                .setMinStartTime(minStartTime).setMaxStartTime(maxStartTime);
        Page<ListViewProcessInstanceDto> page = processInstanceService.pageProcessInstanceByZeebeKey(envId, zeebeKeyList,
                filter.getPageNum(), filter.getPageSize(), processInstanceFilter, new HashMap<>());

        Map<String, ?> extraMap = page.getExtraMap();

        List<OperateProcessInstanceVo> vos = new ArrayList<>();
        for (ListViewProcessInstanceDto listViewProcessInstanceDto : page.getRecords()) {
            String _processId = listViewProcessInstanceDto.getBpmnProcessId();
            OperateProcessInstanceVo vo = OperateProcessInstanceVoConverter.createFrom(listViewProcessInstanceDto);

            SceneReleaseDeployDto sceneReleaseDeployDto = sceneReleaseDeployDtoMap.get(_processId);
            OperateProcessInstanceVoConverter.updateFrom(vo, sceneReleaseDeployDto);

            ProcessReleaseDeployDto processReleaseDeployDto = processReleaseDeployDtoMap.get(_processId);
            OperateProcessInstanceVoConverter.updateFrom(vo, processReleaseDeployDto);
            vos.add(vo);
        }

        Page<OperateProcessInstanceVo> ret = new Page<>(page.getTotal(), vos);
        ret.setExtraMap(extraMap);
        return ret;
    }

    public ProcessDefinitionVo definitionProcessInstance(OperateProcessInstanceVo vo) {
        String processId = vo.getProcessId();
        VersionProcessDto versionProcessDto =
                versionProcessAService.fetchOneByProcessId(ListUtil.of(vo.getProcessId())).get(processId);
        Assert.notNull(versionProcessDto, "??????id?????????");

        return ProcessDefinitionVoConverter.createFrom(vo.getProcessId(), vo.getProcessName(), versionProcessDto.getProcessXml());
    }

    public SceneStatistic statistic(SceneDeployFilter filter) {
        SceneStatistic ret = new SceneStatistic();
        Long envId = Assert.notNull(filter.getEnvId());
        //???????????????????????????

        LocalDateTime minStartTime = Opt.ofNullable(filter.getStartTime()).map(time -> DateUtil.parse(time).toLocalDateTime()).orElse(LocalDateTime.now().minusDays(1));
        LocalDateTime maxStartTime = Opt.ofNullable(filter.getEndTime()).map(time -> DateUtil.parse(time).toLocalDateTime()).orElse(LocalDateTime.now());


        // ?????????????????????????????????id??????
        List<SceneReleaseDeployDto> sceneReleaseDeployDtoList = fetchSceneReleaseDeployDtoList(filter);
        Map<String, SceneReleaseDeployDto> sceneReleaseDeployDtoMap = sceneReleaseDeployDtoList.stream()
                .collect(Collectors.toMap(SceneReleaseDeployDto::getProcessId, Function.identity()));

        // ?????? process release deploy??????
        Map<String, ProcessReleaseDeployDto> processReleaseDeployDtoMap =
                processDeployService.fetchReleaseByEnvIdAndProcessId(envId, ListUtil.toList(sceneReleaseDeployDtoMap.keySet()));

        Map<Long, ProcessReleaseDeployDto> zeebeKeyMap = processReleaseDeployDtoMap.values().stream()
                .collect(Collectors.toMap(ProcessReleaseDeployDto::getProcessZeebeKey, Function.identity()));

        ProcessInstanceFilter processInstanceFilter = new ProcessInstanceFilter()
                .setMinStartTime(minStartTime).setMaxStartTime(maxStartTime);

        List<ProcessInstanceKeyAgg> processInstanceKeyAggList = processInstanceService
                .aggProcessInstanceKey(envId, ListUtil.toList(zeebeKeyMap.keySet()), processInstanceFilter);

        int executionCount = 0;
        int successCount = 0;
        int failCount = 0;
        Map<String, Integer> executionSceneRankingMap = new HashMap<>();
        Map<String, Integer> executionSceneRankingSuccessMap = new HashMap<>();
        Map<String, Integer> executionSceneRankingFailMap = new HashMap<>();
        Map<LocalDate, Integer> executionSceneTendencyMap = new HashMap<>();
        Map<LocalDate, Integer> executionSceneTendencySuccessMap = new HashMap<>();
        Map<LocalDate, Integer> executionSceneTendencyFailMap = new HashMap<>();
        for (ProcessInstanceKeyAgg agg : processInstanceKeyAggList) {
            Integer docCount = agg.getDocCount();
            String state = agg.getState();
            Boolean incident = agg.getIncident();
            Long zeebeKey = agg.getProcessInstanceKey();
            LocalDate startDate = agg.getStartDate();
            ProcessInstanceStateDto _state = null;
            if(StrUtil.equals(state, ProcessInstanceState.COMPLETED.name())) {
                _state = ProcessInstanceStateDto.COMPLETED;
            } else if (StrUtil.equals(state, ProcessInstanceState.ACTIVE.name()) && incident) {
                _state = ProcessInstanceStateDto.INCIDENT;
            }

            executionCount += docCount;

            if(_state == ProcessInstanceStateDto.COMPLETED) {
                successCount += docCount;
            } else if (_state == ProcessInstanceStateDto.INCIDENT) {
                failCount += docCount;
            }

            if(_state == ProcessInstanceStateDto.COMPLETED || _state == ProcessInstanceStateDto.INCIDENT) {
                String processId = zeebeKeyMap.get(zeebeKey).getProcessId();
                SceneReleaseDeployDto sceneReleaseDeployDto = sceneReleaseDeployDtoMap.get(processId);
                String sceneName = sceneReleaseDeployDto.getSceneName();
                executionSceneRankingMap.put(sceneName, executionSceneRankingMap.getOrDefault(sceneName, 0) + docCount);

                if(_state == ProcessInstanceStateDto.COMPLETED) {
                    executionSceneRankingSuccessMap.put(sceneName, executionSceneRankingSuccessMap.getOrDefault(sceneName, 0) + docCount);
                } else {
                    executionSceneRankingFailMap.put(sceneName, executionSceneRankingFailMap.getOrDefault(sceneName, 0) + docCount);
                }

                executionSceneTendencyMap.put(startDate, executionSceneTendencyMap.getOrDefault(startDate, 0) + docCount);
                if(_state == ProcessInstanceStateDto.COMPLETED) {
                    executionSceneTendencySuccessMap.put(startDate, executionSceneTendencySuccessMap.getOrDefault(startDate, 0) + 1);
                } else {
                    executionSceneTendencyFailMap.put(startDate, executionSceneTendencyFailMap.getOrDefault(startDate, 0) + 1);
                }
            }
        }

        // assemble result
        assembleCount(ret, executionCount, successCount, failCount);
        assembleExecutionSceneRanking(ret, executionSceneRankingMap, executionSceneRankingSuccessMap, executionSceneRankingFailMap);
        assembleExecutionSceneTendency(ret, executionSceneTendencyMap, executionSceneTendencySuccessMap, executionSceneTendencyFailMap);
        return ret;
    }

    private List<SceneReleaseDeployDto> fetchSceneReleaseDeployDtoList(SceneDeployFilter filter) {
        Long envId = Assert.notNull(filter.getEnvId());

        Long projectId = Opt.ofNullable(filter.getProjectId()).map(Long::valueOf).orElse(null);
        Long sceneId = filter.getSceneId();
        Long versionId = filter.getVersionId();
        String processId = filter.getProcessId();

        List<SceneReleaseDeployDto> sceneReleaseDeployDtoList = sceneReleaseDeployService.fetchByEnvId(envId);
        sceneReleaseDeployDtoList = sceneReleaseDeployDtoList.stream().filter(dto -> {
            if (sceneId == null) return true;
            if (!NumberUtil.equals(sceneId, dto.getSceneId())) return false;

            if (versionId == null) return true;
            if (!NumberUtil.equals(versionId, dto.getVersionId())) return false;

            if (processId == null) return true;
            if (!StrUtil.equals(processId, dto.getProcessId())) {
                return false;
            } else {
                return true;
            }
        }).collect(Collectors.toList());

        List<Long> sceneIdList = sceneReleaseDeployDtoList.stream().map(SceneReleaseDeployDto::getSceneId)
                .distinct().collect(Collectors.toList());

        Map<Long, SceneDto> sceneDtoMap = sceneService.fetchById(sceneIdList);


        sceneReleaseDeployDtoList = sceneReleaseDeployDtoList.stream().filter(dto -> {
            SceneDto sceneDto = sceneDtoMap.get(dto.getSceneId());
            if(projectId != null && !projectId.equals(sceneDto.getProjectId())) return false;
            return true;
        }).collect(Collectors.toList());

        return sceneReleaseDeployDtoList;
    }

    private void assembleCount(SceneStatistic statistic, int executionCount, int successCount, int failCount) {
        statistic.setExecutionCount(executionCount);
        statistic.setSuccessCount(successCount);
        statistic.setFailCount(failCount);
    }

    private void assembleExecutionSceneRanking(SceneStatistic statistic,
                                               Map<String, Integer> executionSceneRankingMap,
                                               Map<String, Integer> executionSceneRankingSuccessMap,
                                               Map<String, Integer> executionSceneRankingFailMap) {
        ChartOption chart = new ChartOption();
        List<Pair<String, Integer>> executionSceneRankingList = executionSceneRankingMap.entrySet().stream()
                .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                .sorted((o1, o2) -> NumberUtil.compare(o2.getValue(), o1.getValue()))
                .collect(Collectors.toList());
        List<String> categoryList = new ArrayList<>();
        List<Integer> successDataList = new ArrayList<>();
        List<Integer> falseDataList = new ArrayList<>();

        for (int i = 0; i < executionSceneRankingList.size() && i < MAX_SIZE; i++) {
            Pair<String, Integer> pair = executionSceneRankingList.get(i);
            String name = pair.getKey();
            Integer successCount = executionSceneRankingSuccessMap.getOrDefault(name, 0);
            Integer falseCount = executionSceneRankingFailMap.getOrDefault(name, 0);
            categoryList.add(name);
            successDataList.add(successCount);
            falseDataList.add(falseCount);
        }

        chart.setCategory(categoryList.toArray());
        Series seriesSuccess = new Series("??????????????????", successDataList.toArray());
        Series seriesFalse = new Series("??????????????????", falseDataList.toArray());
        chart.setSeries(new Series[]{seriesSuccess, seriesFalse});
        statistic.setExecutionSceneRanking(chart);
    }

    private void assembleExecutionSceneTendency(SceneStatistic statistic,
                                                Map<LocalDate, Integer> executionSceneTendencyMap,
                                                Map<LocalDate, Integer> executionSceneTendencySuccessMap,
                                                Map<LocalDate, Integer> executionSceneTendencyFailMap) {
        ChartOption chart = new ChartOption();
        List<Pair<LocalDate, Integer>> executionSceneTendencyList = executionSceneTendencyMap.entrySet().stream()
                .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                .sorted((o1, o2) -> o2.getKey().compareTo(o1.getKey()))
                .collect(Collectors.toList());
        List<String> categoryList = new ArrayList<>();
        List<Integer> successDataList = new ArrayList<>();
        List<Integer> falseDataList = new ArrayList<>();

        for (int i = 0; i < executionSceneTendencyList.size() && i < MAX_SIZE; i++) {
            Pair<LocalDate, Integer> pair = executionSceneTendencyList.get(i);
            LocalDate date = pair.getKey();
            Integer successCount = executionSceneTendencySuccessMap.getOrDefault(date, 0);
            Integer falseCount = executionSceneTendencyFailMap.getOrDefault(date, 0);
            categoryList.add(LocalDateTimeUtil.formatNormal(date));
            successDataList.add(successCount);
            falseDataList.add(falseCount);
        }

        chart.setCategory(categoryList.toArray());
        Series seriesSuccess = new Series("??????????????????", successDataList.toArray());
        Series seriesFalse = new Series("??????????????????", falseDataList.toArray());
        chart.setSeries(new Series[]{seriesSuccess, seriesFalse});

        statistic.setExecutionSceneTendency(chart);
    }

//    private boolean checkTimeInterval(LocalDateTime time1, LocalDateTime time2){
//        Duration duration = Duration.between(time1,time2);
//        Long monthMillis = 2592000000L;
//        Long dMillis = duration.toMillis();
//        if(Math.abs(dMillis)>monthMillis){
//            return false;
//        }
//        return true;
//    }

}