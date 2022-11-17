package com.brandnewdata.mop.poc.bff.service.sceneOperate;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.bff.model.sceneOperate.condition.Process;
import com.brandnewdata.mop.poc.bff.model.sceneOperate.condition.Scene;
import com.brandnewdata.mop.poc.bff.model.sceneOperate.condition.Version;
import com.brandnewdata.mop.poc.process.dto.ProcessDeployDto;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService;
import com.brandnewdata.mop.poc.scene.dto.SceneDto2;
import com.brandnewdata.mop.poc.scene.dto.SceneProcessDto2;
import com.brandnewdata.mop.poc.scene.service.ISceneProcessService;
import com.brandnewdata.mop.poc.scene.service.ISceneService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.brandnewdata.mop.poc.util.CollectorsUtil.toSortedList;
import static java.util.stream.Collectors.groupingBy;

@Service
public class SceneOperateService {
    @Resource
    private IProcessDeployService processDeployService;

    @Resource
    private ISceneProcessService sceneProcessService;

    @Resource
    private ISceneService sceneService;


    public Result<List<Scene>> getAllScene() {
        // 获取所有场景的部署流程列表
        List<ProcessDeployDto> processDeployDtoList = processDeployService.listAll(1);

        // 根据process id获取关联的场景
        List<String> processIdList = processDeployDtoList.stream()
                .map(ProcessDeployDto::getProcessId).collect(Collectors.toList());
        List<SceneProcessDto2> sceneProcessDto2List = sceneProcessService.listByProcessIdList(processIdList);

        // 根据 scene id 获取scene
        List<Long> sceneIdList = sceneProcessDto2List.stream().map(SceneProcessDto2::getSceneId)
                .distinct().collect(Collectors.toList());
        List<SceneDto2> sceneDto2List = sceneService.listByIdList(sceneIdList);

        // 根据process id 分组，再根据版本排序
        Map<String, List<ProcessDeployDto>> processDeployMap = processDeployDtoList.stream().collect(groupingBy(ProcessDeployDto::getProcessId,

                toSortedList(Comparator.comparingInt(ProcessDeployDto::getVersion))));

        // 根据场景分组，再根据最新版本的更新时间排序
        Map<Long, List<SceneProcessDto2>> sceneMap = sceneProcessDto2List.stream().collect(groupingBy(SceneProcessDto2::getSceneId,
                toSortedList((o1, o2) -> {
                    LocalDateTime time1 = processDeployMap.get(o1.getProcessId()).get(0).getUpdateTime();
                    LocalDateTime time2 = processDeployMap.get(o2.getProcessId()).get(0).getUpdateTime();
                    return time1.compareTo(time2);
                })));


        // 根据场景下的流程，再根据流程最新版本进行排序
        List<SceneDto2> sceneSortedList = sceneDto2List.stream().collect(toSortedList(new Comparator<SceneDto2>() {
            @Override
            public int compare(SceneDto2 o1, SceneDto2 o2) {
                LocalDateTime time1 = processDeployMap.get(sceneMap.get(o1.getId()).get(0).getProcessId()).get(0).getCreateTime();
                LocalDateTime time2 = processDeployMap.get(sceneMap.get(o2.getId()).get(0).getProcessId()).get(0).getCreateTime();
                return time1.compareTo(time2);
            }
        }));






        return null;
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
            version.setVersion(processDeployDto.getVersion());
            version.setCreateTime(processDeployDto.getCreateTime());
            versionList.add(version);
        }
        return versionList;
    }



}
