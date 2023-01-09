package com.brandnewdata.mop.poc.scene.scheduler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.Scheduler;
import com.brandnewdata.mop.poc.constant.ProcessConst;
import com.brandnewdata.mop.poc.constant.SceneConst;
import com.brandnewdata.mop.poc.env.dto.EnvDto;
import com.brandnewdata.mop.poc.env.service.IEnvService;
import com.brandnewdata.mop.poc.process.dto.BpmnXmlDto;
import com.brandnewdata.mop.poc.process.dto.DeployStatusDto;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionParseDto;
import com.brandnewdata.mop.poc.process.manager.ConnectorManager;
import com.brandnewdata.mop.poc.process.service.IProcessDefinitionService;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService;
import com.brandnewdata.mop.poc.scene.dto.*;
import com.brandnewdata.mop.poc.scene.service.atomic.ISceneReleaseDeployAService;
import com.brandnewdata.mop.poc.scene.service.atomic.ISceneVersionAService;
import com.brandnewdata.mop.poc.scene.service.atomic.IVersionProcessAService;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SceneDeployProgressScheduler {

    private final ISceneReleaseDeployAService sceneReleaseDeployAService;

    private final ISceneVersionAService sceneVersionAService;

    private final IVersionProcessAService versionProcessAService;

    private final IProcessDeployService processDeployService;

    private final IEnvService envService;

    private final IProcessDefinitionService processDefinitionService;

    private final ConnectorManager connectorManager;

    public SceneDeployProgressScheduler(ISceneReleaseDeployAService sceneReleaseDeployAService,
                                        ISceneVersionAService sceneVersionAService,
                                        IVersionProcessAService versionProcessAService,
                                        IProcessDeployService processDeployService,
                                        IEnvService envService,
                                        IProcessDefinitionService processDefinitionService,
                                        ConnectorManager connectorManager) {
        this.sceneReleaseDeployAService = sceneReleaseDeployAService;
        this.sceneVersionAService = sceneVersionAService;
        this.versionProcessAService = versionProcessAService;
        this.processDeployService = processDeployService;
        this.envService = envService;
        this.processDefinitionService = processDefinitionService;
        this.connectorManager = connectorManager;
        Scheduler scheduler = new Scheduler();
        scheduler.setMatchSecond(true);
        scheduler.schedule("0/4 * * * * ?", (Runnable) this::scan);
        scheduler.start();
    }

    private void scan() {
        List<SceneVersionDto> sceneVersionDtoList = sceneVersionAService.fetchAllUnDeployed();
        for (SceneVersionDto sceneVersionDto : sceneVersionDtoList) {
            Integer deployStatus = sceneVersionDto.getDeployStatus();
            if(NumberUtil.equals(SceneConst.SCENE_DEPLOY_STATUS_SNAPSHOT_UNDEPLOY, deployStatus)) {
                snapshotDeployProgress(sceneVersionDto);
            } else if (NumberUtil.equals(SceneConst.SCENE_DEPLOY_STATUS_RELEASE_UNDEPLOY, deployStatus)) {
                releaseDeployProgress(sceneVersionDto);
            }
        }
    }

    private void snapshotDeployProgress(SceneVersionDto sceneVersionDto) {
        Long versionId = sceneVersionDto.getId();
        EnvDto envDto = envService.fetchDebugEnv();
        List<VersionProcessDto> versionProcessDtoList =
                versionProcessAService.fetchListByVersionId(ListUtil.of(versionId), true).get(versionId);
        List<String> processIdList = versionProcessDtoList.stream().map(VersionProcessDto::getProcessId).collect(Collectors.toList());

        // 查询版本的部署进度
        SceneVersionDeployProgressDto sceneVersionDeployProgressDto = getSceneVersionDeployProgressDto(processIdList, ListUtil.of(envDto.getId()));

        int status = sceneVersionDeployProgressDto.getStatus();
        double progressPercentage = sceneVersionDeployProgressDto.getProgressPercentage();
        String errorMessage = sceneVersionDeployProgressDto.getErrorMessage();

        if(NumberUtil.equals(status, ProcessConst.PROCESS_DEPLOY_STATUS__UNDEPLOY)) {
            sceneVersionDto.setDeployStatus(SceneConst.SCENE_DEPLOY_STATUS_SNAPSHOT_UNDEPLOY);
            sceneVersionDto.setDeployProgressPercentage(progressPercentage);
            sceneVersionAService.save(sceneVersionDto);
            return;
        }

        if (NumberUtil.equals(status, ProcessConst.PROCESS_DEPLOY_STATUS__EXCEPTION)) {
            sceneVersionDto.setDeployStatus(SceneConst.SCENE_DEPLOY_STATUS__EXCEPTION);
            sceneVersionDto.setDeployProgressPercentage(progressPercentage);
            sceneVersionDto.setExceptionMessage(errorMessage);
            sceneVersionAService.save(sceneVersionDto);
            return;
        }

        if (NumberUtil.equals(status, ProcessConst.PROCESS_DEPLOY_STATUS__DEPLOYED)) {
            sceneVersionDto.setDeployStatus(SceneConst.SCENE_DEPLOY_STATUS__DEPLOYED);
            sceneVersionDto.setStatus(SceneConst.SCENE_VERSION_STATUS__DEBUGGING);
            sceneVersionDto.setDeployProgressPercentage(progressPercentage);
            sceneVersionAService.save(sceneVersionDto);
        }
    }

    private void releaseDeployProgress(SceneVersionDto sceneVersionDto) {
        Long versionId = sceneVersionDto.getId();
        List<SceneReleaseDeployDto> sceneReleaseDeployDtoList = sceneReleaseDeployAService.fetchListByVersionId(ListUtil.of(versionId)).get(versionId);
        Map<Long, Map<String, SceneReleaseDeployDto>> envProcessMapMap = sceneReleaseDeployDtoList.stream()
                .collect(Collectors.groupingBy(SceneReleaseDeployDto::getEnvId, Collectors.toMap(SceneReleaseDeployDto::getProcessId, Function.identity())));
        List<Long> envList = ListUtil.toList(envProcessMapMap.keySet());
        List<String> processIdList = envProcessMapMap.values().stream().flatMap(map -> map.keySet().stream()).distinct().collect(Collectors.toList());
        SceneVersionDeployProgressDto sceneVersionDeployProgressDto = getSceneVersionDeployProgressDto(processIdList, envList);
        int status = sceneVersionDeployProgressDto.getStatus();
        double progressPercentage = sceneVersionDeployProgressDto.getProgressPercentage();
        String errorMessage = sceneVersionDeployProgressDto.getErrorMessage();

        if(NumberUtil.equals(status, ProcessConst.PROCESS_DEPLOY_STATUS__UNDEPLOY)) {
            sceneVersionDto.setDeployStatus(SceneConst.SCENE_DEPLOY_STATUS_RELEASE_UNDEPLOY);
            sceneVersionDto.setDeployProgressPercentage(progressPercentage);
            sceneVersionAService.save(sceneVersionDto);
            return;
        }

        if (NumberUtil.equals(status, ProcessConst.PROCESS_DEPLOY_STATUS__EXCEPTION)) {
            sceneVersionDto.setDeployStatus(SceneConst.SCENE_DEPLOY_STATUS__EXCEPTION);
            sceneVersionDto.setDeployProgressPercentage(progressPercentage);
            sceneVersionDto.setExceptionMessage(errorMessage);
            sceneVersionAService.save(sceneVersionDto);
            return;
        }

        if (NumberUtil.equals(status, ProcessConst.PROCESS_DEPLOY_STATUS__DEPLOYED)) {
            // 保存request params
            saveRequestParams(processIdList, envList, envProcessMapMap);

            sceneVersionDto.setStatus(SceneConst.SCENE_VERSION_STATUS__RUNNING);
            sceneVersionDto.setDeployStatus(SceneConst.SCENE_DEPLOY_STATUS__DEPLOYED);
            sceneVersionDto.setDeployProgressPercentage(progressPercentage);
            sceneVersionAService.save(sceneVersionDto);
        }

    }

    private SceneVersionDeployProgressDto getSceneVersionDeployProgressDto(List<String> processIdList, List<Long> envIdList) {
        SceneVersionDeployProgressDto ret = new SceneVersionDeployProgressDto();

        if(CollUtil.isEmpty(processIdList)) {
            ret.setStatus(ProcessConst.PROCESS_DEPLOY_STATUS__DEPLOYED);
            ret.setProgressPercentage(100.00);
            ret.setProcessDeployStatusMap(MapUtil.empty());
            return ret;
        }

        Map<String, ProcessDeployProgressDto> processDeployProgressDtoMap = new HashMap<>();

        for (Long envId : Opt.ofNullable(envIdList).orElse(ListUtil.empty())) {
            Map<String, DeployStatusDto> deployStatusDtoMap =
                    processDeployService.fetchDeployStatus(processIdList, envId);
            Assert.isTrue(deployStatusDtoMap.size() == processIdList.size(), "资源尚未部署，无法查询状态");

            for (Map.Entry<String, DeployStatusDto> entry : deployStatusDtoMap.entrySet()) {
                String processId = entry.getKey();
                DeployStatusDto _deployStatusDto = entry.getValue();
                ProcessDeployProgressDto processDeployProgressDto =
                        processDeployProgressDtoMap.computeIfAbsent(processId, key ->
                                new ProcessDeployProgressDto(ProcessConst.PROCESS_DEPLOY_STATUS__DEPLOYED, new HashMap<>()));

                if(_deployStatusDto.getStatus() == ProcessConst.PROCESS_DEPLOY_STATUS__EXCEPTION) {
                    // 如果当前部署是失败，则不管之前是什么状态，都修改为失败
                    processDeployProgressDto.setStatus(_deployStatusDto.getStatus());

                    // 记录异常结果
                    EnvDto envDto = envService.fetchOne(envId);
                    Map<String, String> messageMap =
                            Opt.ofNullable(processDeployProgressDto.getErrorMessageMap()).orElse(new LinkedHashMap<>());
                    messageMap.put(envDto.getName(), _deployStatusDto.getMessage());
                    processDeployProgressDto.setErrorMessageMap(messageMap);
                } else if (processDeployProgressDto.getStatus() == ProcessConst.PROCESS_DEPLOY_STATUS__DEPLOYED) { // 默认赋值为 1
                    // 如果之前是成功部署的，则当前环境状态可以直接覆盖总状态
                    processDeployProgressDto.setStatus(_deployStatusDto.getStatus());
                }
            }
        }

        int status = 1;
        int totalCount = 0;
        int successCount = 0;
        List<String> errorMessages = new ArrayList<>();
        for (ProcessDeployProgressDto processDeployProgressDto : processDeployProgressDtoMap.values()) {
            int _status = processDeployProgressDto.getStatus();
            if(NumberUtil.equals(_status, ProcessConst.PROCESS_DEPLOY_STATUS__EXCEPTION)) {
                status = _status;

                Map<String, String> _errorMessageMap = processDeployProgressDto.getErrorMessageMap();
                _errorMessageMap.forEach((envName, message) -> {
                    errorMessages.add(StrUtil.format("{}: {}", envName, message));
                });
            } else if (NumberUtil.equals(status, ProcessConst.PROCESS_DEPLOY_STATUS__DEPLOYED)) {
                status = _status;
            }

            totalCount++;
            if(NumberUtil.equals(_status, ProcessConst.PROCESS_DEPLOY_STATUS__DEPLOYED)) {
                successCount++;
            }
        }

        ret.setStatus(status);
        ret.setErrorMessage(StrUtil.join("; ", errorMessages));
        ret.setProgressPercentage(NumberUtil.div(100 * successCount, totalCount, 2));

        return ret;
    }

    private void saveRequestParams(List<String> processIdList, List<Long> envIdList, Map<Long, Map<String, SceneReleaseDeployDto>> envProcessMapMap) {
        for (String processId : processIdList) {
            VersionProcessDto versionProcessDto = versionProcessAService.fetchOneByProcessId(ListUtil.of(processId)).get(processId);
            BpmnXmlDto deployDto = new BpmnXmlDto();
            deployDto.setProcessId(versionProcessDto.getProcessId());
            deployDto.setProcessName(versionProcessDto.getProcessName());
            deployDto.setProcessXml(versionProcessDto.getProcessXml());

            ProcessDefinitionParseDto processDefinitionParseDto = processDefinitionService.parseSceneTrigger(deployDto);

            for (Long envId : envIdList) {
                if(StrUtil.isNotBlank(processDefinitionParseDto.getTriggerFullId())) {
                    SceneReleaseDeployDto sceneReleaseDeployDto = envProcessMapMap.get(envId).get(processId);
                    // 有触发器时，才需要上报
                    connectorManager.saveRequestParams(envId, processDefinitionParseDto.getTriggerFullId(),
                            processDefinitionParseDto.getProtocol(), processDefinitionParseDto.getRequestParams(),
                            sceneReleaseDeployDto);
                }
            }
        }
    }

}
