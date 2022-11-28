package com.brandnewdata.mop.poc.bff.service.operate;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.bff.vo.operate.ProcessInstance;
import com.brandnewdata.mop.poc.bff.vo.operate.Statistic;
import com.brandnewdata.mop.poc.bff.vo.operate.charts.ChartOption;
import com.brandnewdata.mop.poc.bff.vo.operate.charts.Series;
import com.brandnewdata.mop.poc.bff.vo.operate.condition.Filter;
import com.brandnewdata.mop.poc.bff.vo.operate.condition.Process;
import com.brandnewdata.mop.poc.bff.vo.operate.condition.Scene;
import com.brandnewdata.mop.poc.bff.vo.operate.condition.Version;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDto;
import com.brandnewdata.mop.poc.operate.service.IProcessInstanceService;
import com.brandnewdata.mop.poc.process.dto.ProcessDeployDto;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService;
import com.brandnewdata.mop.poc.scene.dto.SceneDto2;
import com.brandnewdata.mop.poc.scene.dto.SceneProcessDto2;
import com.brandnewdata.mop.poc.scene.service.ISceneProcessService;
import com.brandnewdata.mop.poc.scene.service.ISceneService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.brandnewdata.mop.poc.operate.dto.ProcessInstanceStateDto.COMPLETED;
import static com.brandnewdata.mop.poc.operate.dto.ProcessInstanceStateDto.INCIDENT;
import static com.brandnewdata.mop.poc.util.CollectorsUtil.toSortedList;
import static java.util.stream.Collectors.groupingBy;

@Service
public class SceneOperateBffService {
    @Resource
    private IProcessDeployService processDeployService;
    @Resource
    private ISceneProcessService sceneProcessService;
    @Resource
    private ISceneService sceneService;
    @Resource
    private IProcessInstanceService processInstanceService;

    public List<Scene> getAllScene() {
        // 获取所有场景的部署流程列表
        List<ProcessDeployDto> processDeployDtoList = processDeployService.listByType(1);

        // 根据process id获取关联的场景
        List<String> processIdList = processDeployDtoList.stream()
                .map(ProcessDeployDto::getProcessId).collect(Collectors.toList());
        List<SceneProcessDto2> sceneProcessDto2List = sceneProcessService.listByProcessIdList(processIdList);

        // 根据 scene id 获取scene
        List<Long> sceneIdList = sceneProcessDto2List.stream().map(SceneProcessDto2::getSceneId)
                .distinct().collect(Collectors.toList());
        List<SceneDto2> sceneDto2List = sceneService.listByIdList(sceneIdList);

        // 根据process id 分组，再根据版本排序
        Map<String, List<ProcessDeployDto>> processDeployMap = processDeployDtoList.stream()
                .collect(groupingBy(ProcessDeployDto::getProcessId,
                        toSortedList((o1, o2) -> {
                            int version1 = o1.getVersion();
                            int version2 = o2.getVersion();
                            // 倒序
                            return Integer.compare(version2, version1);
                        })));


        // 根据场景分组，再根据最新版本的更新时间排序
        Map<Long, List<SceneProcessDto2>> sceneMap = sceneProcessDto2List.stream().collect(groupingBy(SceneProcessDto2::getSceneId,
                toSortedList((o1, o2) -> {
                    LocalDateTime time1 = processDeployMap.get(o1.getProcessId()).get(0).getCreateTime();
                    LocalDateTime time2 = processDeployMap.get(o2.getProcessId()).get(0).getCreateTime();
                    // 倒序
                    return time2.compareTo(time1);
                })));


        // 根据场景下的流程，再根据流程最新版本进行排序
        List<SceneDto2> sceneSortedList = sceneDto2List.stream().collect(toSortedList(new Comparator<SceneDto2>() {
            @Override
            public int compare(SceneDto2 o1, SceneDto2 o2) {
                LocalDateTime time1 = processDeployMap.get(sceneMap.get(o1.getId()).get(0).getProcessId()).get(0).getCreateTime();
                LocalDateTime time2 = processDeployMap.get(sceneMap.get(o2.getId()).get(0).getProcessId()).get(0).getCreateTime();
                // 倒序
                return time2.compareTo(time1);
            }
        }));

        List<Scene> sceneList = getSceneList(sceneSortedList, sceneMap, processDeployMap);
        return sceneList;
    }

    public Page<ProcessInstance> pageProcessInstance(Filter filter) {
        List<Scene> sceneList = getAllScene();
        Map<ProcessIdAndVersion, ProcessDeployInfo> processIdAndVersionMap = new HashMap<>();
        List<Long> deployIdList = new ArrayList<>();
        for (Scene scene : sceneList) {
            if(filter.getSceneId() != null && ! NumberUtil.equals(filter.getSceneId(), scene.getId())) continue;
            for (Process process : scene.getProcessList()) {
                if (filter.getProcessId() != null && !StrUtil.equals(filter.getProcessId(), process.getProcessId())) continue;
                for (Version version : process.getVersionList()) {
                    if(filter.getVersion() != null && !NumberUtil.equals(filter.getVersion(), version.getVersion())) continue;
                    deployIdList.add(version.getDeployId());
                    ProcessDeployInfo processDeployInfo = new ProcessDeployInfo();
                    processDeployInfo.setSceneId(scene.getId());
                    processDeployInfo.setSceneName(scene.getName());
                    processDeployInfo.setProcessId(process.getProcessId());
                    processDeployInfo.setProcessName(process.getName());
                    processDeployInfo.setDeployId(version.getDeployId());
                    processIdAndVersionMap.put(new ProcessIdAndVersion(process.getProcessId(), version.getVersion()), processDeployInfo);
                }
            }
        }

        List<ProcessDeployDto> processDeployDtoList = processDeployService.listByIdList(deployIdList);

        List<Long> zeebeKeyList = processDeployDtoList.stream().map(ProcessDeployDto::getZeebeKey).collect(Collectors.toList());

        Page<ListViewProcessInstanceDto> listViewProcessInstanceDtoPage =
                processInstanceService.pageProcessInstanceByZeebeKey(zeebeKeyList, filter.getPageNum(), filter.getPageSize(), null);
        List<ProcessInstance> records = listViewProcessInstanceDtoPage.getRecords().stream().map(r -> toProcessInstance(r, processIdAndVersionMap))
                .collect(Collectors.toList());
        Page page = new Page(listViewProcessInstanceDtoPage.getTotal(), records);
        page.setExtraMap(listViewProcessInstanceDtoPage.getExtraMap());
        return page;
    }

    public Statistic statistic(Filter filter) {
        Statistic ret = new Statistic();
        List<Scene> sceneList = getAllScene();
        Map<ProcessIdAndVersion, String[]> processIdAndVersionMap = new HashMap<>();
        for (Scene scene : sceneList) {
            if(filter.getSceneId() != null && ! NumberUtil.equals(filter.getSceneId(), scene.getId())) continue;
            for (Process process : scene.getProcessList()) {
                if (filter.getProcessId() != null && !StrUtil.equals(filter.getProcessId(), process.getProcessId())) continue;
                for (Version version : process.getVersionList()) {
                    if(filter.getVersion() != null && !NumberUtil.equals(filter.getVersion(), version.getVersion())) continue;
                    processIdAndVersionMap.put(new ProcessIdAndVersion(process.getProcessId(), version.getVersion()),
                            new String[]{scene.getName(), process.getName()});
                }
            }
        }

        // 获取所有场景的部署流程列表
        List<ProcessDeployDto> processDeployDtoList = processDeployService.listByType(1);
        Map<ProcessIdAndVersion, ProcessDeployDto> processIdAndVersionMap2 = processDeployDtoList.stream().collect(
                Collectors.toMap(dto -> new ProcessIdAndVersion(dto.getProcessId(), dto.getVersion()), Function.identity()));

        // 获取所有流程实例
        List<ListViewProcessInstanceDto> listViewProcessInstanceDtos = processInstanceService.listAll();

        // 运行次数
        int executionCount = 0;
        // 成功次数
        int successCount = 0;
        // 失败次数
        int failCount = 0;
        // 场景运行次数排名
        Map<String, int[]> executionSceneRankingDataMap = new HashMap<>();
        TreeMap<String, int[]> executionSceneTendencyMap = new TreeMap<>();
        Map<String, Integer> executionTriggerRankingDataMap = new HashMap<>();
        Map<String, Map<String, Integer>> executionTriggerTendencyDataMap = new TreeMap<>();

        for (ListViewProcessInstanceDto dto : listViewProcessInstanceDtos) {
            String processId = dto.getBpmnProcessId();
            Integer version = dto.getProcessVersion();
            ProcessIdAndVersion processIdAndVersion = new ProcessIdAndVersion(processId, version);
            ProcessDeployDto processDeployDto = processIdAndVersionMap2.get(processIdAndVersion);
            String[] names = processIdAndVersionMap.get(processIdAndVersion);
            String date = LocalDateTimeUtil.format(dto.getStartDate(), "MM-dd");

            if(names == null) continue;
            String sceneName = names[0];
            int[] executionSceneRankingData  = executionSceneRankingDataMap.computeIfAbsent(sceneName, key -> new int[]{0, 0});

            int[] executionSceneTendencyData = executionSceneTendencyMap.computeIfAbsent(date, key -> new int[]{0, 0});

            String trigger = Opt.ofNullable(processDeployDto.getTriggerType()).orElse("未知触发器");
            Integer triggerCount = executionTriggerRankingDataMap.computeIfAbsent(trigger, key -> 0);

            executionCount += 1;
            triggerCount += 1;
            Map<String, Integer> triggerTendencyDataMap = executionTriggerTendencyDataMap.computeIfAbsent(date, key -> new HashMap<>());
            Integer triggerTendencyCount = triggerTendencyDataMap.computeIfAbsent(trigger, key -> 0);
            triggerTendencyCount += 1;

            if(dto.getState() == COMPLETED) {
                successCount += 1;
                executionSceneRankingData[0] += 1;
                executionSceneTendencyData[0] += 1;
            } else if(dto.getState() == INCIDENT) {
                failCount += 1;
                executionSceneRankingData[1] += 1;
                executionSceneTendencyData[1] += 1;
            }
        }

        // 场景运行次数趋势
        ChartOption executionSceneRanking = getExecutionSceneRanking(executionSceneRankingDataMap);
        // 触发次数分布图
        ChartOption executionSceneTendency = getExecutionSceneTendency(executionSceneTendencyMap);

        ret.setExecutionCount(executionCount);
        ret.setSuccessCount(successCount);
        ret.setFailCount(failCount);
        ret.setExecutionSceneRanking(executionSceneRanking);
        ret.setExecutionSceneTendency(executionSceneTendency);
        ret.setExecutionTriggerDis(new ChartOption());
        ret.setExecutionTriggerTendency(new ChartOption());

        return ret;
    }

    private ChartOption getExecutionSceneRanking(Map<String, int[]> executionSceneRankingDataMap) {
        // 场景运行次数排名
        ChartOption ret = new ChartOption();
        List<Map.Entry<String, int[]>> entries = executionSceneRankingDataMap.entrySet().stream().sorted((o1, o2) -> {
            int sum1 = Arrays.stream(o1.getValue()).sum();
            int sum2 = Arrays.stream(o2.getValue()).sum();
            // 倒序
            return Integer.compare(sum2, sum1);
        }).limit(5).collect(Collectors.toList());
        List<String> categoryList = new ArrayList<>();
        List<Integer> successList = new ArrayList<>();
        List<Integer> failList = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : entries) {
            categoryList.add(entry.getKey());
            int[] nums = entry.getValue();
            successList.add(nums[0]);
            failList.add(nums[1]);
        }

        ret.setCategory(categoryList.toArray());
        Series series1 = new Series("运行成功次数", successList.toArray());
        Series series2 = new Series("运行失败次数", failList.toArray());
        ret.setSeries(new Series[]{series1, series2});
        return ret;
    }

    private ChartOption getExecutionSceneTendency(TreeMap<String, int[]> executionSceneTendencyMap) {
        ChartOption ret = new ChartOption();
        List<String> categoryList = new ArrayList<>();
        List<Integer> successList = new ArrayList<>();
        List<Integer> failList = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : executionSceneTendencyMap.entrySet()) {
            categoryList.add(entry.getKey());
            int[] nums = entry.getValue();
            successList.add(nums[0]);
            failList.add(nums[1]);
        }

        ret.setCategory(categoryList.toArray());
        Series series1 = new Series("运行成功次数", successList.toArray());
        Series series2 = new Series("运行失败次数", failList.toArray());
        ret.setSeries(new Series[]{series1, series2});
        return ret;
    }

    private List<Map.Entry<String, Integer>> sortedTriggerRanking(Map<String, Integer> triggerRankingDataMap) {
        List<Map.Entry<String, Integer>> entries = triggerRankingDataMap.entrySet().stream().sorted((o1, o2) -> {
            Integer num1 = o1.getValue();
            Integer num2 = o1.getValue();
            return Integer.compare(num2, num1);
        }).collect(Collectors.toList());
        if(entries.size() > 10) {
            long sum = entries.stream().skip(9).mapToLong(Map.Entry::getValue).sum();
            entries = entries.stream().limit(9).collect(Collectors.toList());
            entries.add(MapUtil.entry("其他", (int)sum));
        }
        return entries;
    }

    @Getter
    @Setter
    private static class ProcessIdAndVersion {
        private String processId;
        private int version;

        public ProcessIdAndVersion(String processId, int version) {
            this.processId = processId;
            this.version = version;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ProcessIdAndVersion that = (ProcessIdAndVersion) o;

            if (version != that.version) return false;
            return processId.equals(that.processId);
        }

        @Override
        public int hashCode() {
            int result = processId.hashCode();
            result = 31 * result + version;
            return result;
        }
    }

    @Getter
    @Setter
    private static class ProcessDeployInfo {
        private Long sceneId;
        private String sceneName;
        private String processId;
        private String processName;
        private Long deployId;
    }

    private List<Scene> getSceneList(List<SceneDto2> sceneSortedList,
                                     Map<Long, List<SceneProcessDto2>> sceneMap,
                                     Map<String, List<ProcessDeployDto>> processDeployMap) {
        List<Scene> ret = new ArrayList<>();
        for (SceneDto2 sceneDto2 : sceneSortedList) {
            Scene scene = new Scene();
            Long id = sceneDto2.getId();
            scene.setId(id);
            scene.setName(sceneDto2.getName());
            // 添加 processList
            List<Process> processList = getProcessList(sceneMap.get(id), processDeployMap);
            scene.setProcessList(processList);

            ret.add(scene);
        }
        return ret;
    }

    private List<Process> getProcessList(List<SceneProcessDto2> sceneProcessDto2List,
                                         Map<String, List<ProcessDeployDto>> processDeployMap) {
        List<Process> processList = new ArrayList<>();
        for (SceneProcessDto2 sceneProcessDto2 : sceneProcessDto2List) {
            Process process = new Process();
            String processId = sceneProcessDto2.getProcessId();
            List<ProcessDeployDto> _processDeployDtoList = processDeployMap.get(processId);
            String processName = _processDeployDtoList.get(0).getProcessName();
            process.setProcessId(processId);
            process.setName(processName);
            // 添加versionList
            List<Version> versionList = getVersionList(processDeployMap.get(processId));
            process.setVersionList(versionList);

            processList.add(process);
        }
        return processList;
    }

    private List<Version> getVersionList(List<ProcessDeployDto> processDeployDtoList) {
        List<Version> versionList = new ArrayList<>();
        for (ProcessDeployDto processDeployDto : processDeployDtoList) {
            Version version = new Version();
            version.setDeployId(processDeployDto.getId());
            version.setVersion(processDeployDto.getVersion());
            version.setCreateTime(processDeployDto.getCreateTime());
            versionList.add(version);
        }
        return versionList;
    }

    private ProcessInstance toProcessInstance(ListViewProcessInstanceDto listViewProcessInstanceDto,
                                              Map<ProcessIdAndVersion, ProcessDeployInfo> processIdAndVersionMap) {
        String processId = listViewProcessInstanceDto.getBpmnProcessId();
        Integer version = listViewProcessInstanceDto.getProcessVersion();
        ProcessDeployInfo processDeployInfo = processIdAndVersionMap.get(new ProcessIdAndVersion(processId, version));

        ProcessInstance ret = new ProcessInstance();

        ret.setInstanceId(listViewProcessInstanceDto.getId());
        ret.setSceneId(processDeployInfo.getSceneId());
        ret.setSceneName(processDeployInfo.getSceneName());
        ret.setProcessId(processId);
        ret.setProcessName(processDeployInfo.getProcessName());
        ret.setDeployId(processDeployInfo.getDeployId());
        ret.setVersion(version);
        ret.setState(listViewProcessInstanceDto.getState().name());
        ret.setStartTime(Opt.ofNullable(listViewProcessInstanceDto.getStartDate())
                .map(LocalDateTimeUtil::formatNormal).orElse(null));
        ret.setEndTime(Opt.ofNullable(listViewProcessInstanceDto.getEndDate())
                .map(LocalDateTimeUtil::formatNormal).orElse(null));
        return ret;
    }
}