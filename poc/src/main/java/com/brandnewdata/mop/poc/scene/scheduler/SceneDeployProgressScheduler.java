package com.brandnewdata.mop.poc.scene.scheduler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.cron.Scheduler;
import com.brandnewdata.mop.api.connector.dto.BPMNResource;
import com.brandnewdata.mop.api.connector.dto.ConnectorProcessDeployStatusDto;
import com.brandnewdata.mop.poc.constant.ProcessConst;
import com.brandnewdata.mop.poc.constant.SceneConst;
import com.brandnewdata.mop.poc.env.dto.EnvDto;
import com.brandnewdata.mop.poc.env.service.IEnvService;
import com.brandnewdata.mop.poc.process.dto.DeployStatusDto;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService;
import com.brandnewdata.mop.poc.process.util.ProcessUtil;
import com.brandnewdata.mop.poc.scene.dto.ProcessDeployProgressDto;
import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;
import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;
import com.brandnewdata.mop.poc.scene.service.atomic.ISceneReleaseDeployAService;
import com.brandnewdata.mop.poc.scene.service.atomic.ISceneVersionAService;
import com.brandnewdata.mop.poc.scene.service.atomic.IVersionProcessAService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SceneDeployProgressScheduler {

    private final ISceneReleaseDeployAService sceneReleaseDeployAService;

    private final ISceneVersionAService sceneVersionAService;

    private final IVersionProcessAService versionProcessAService;

    private final IProcessDeployService processDeployService;

    private final IEnvService envService;


    public SceneDeployProgressScheduler(ISceneReleaseDeployAService sceneReleaseDeployAService,
                                        ISceneVersionAService sceneVersionAService,
                                        IVersionProcessAService versionProcessAService,
                                        IProcessDeployService processDeployService,
                                        IEnvService envService) {
        this.sceneReleaseDeployAService = sceneReleaseDeployAService;
        this.sceneVersionAService = sceneVersionAService;
        this.versionProcessAService = versionProcessAService;
        this.processDeployService = processDeployService;
        this.envService = envService;
        Scheduler scheduler = new Scheduler();
        scheduler.setMatchSecond(true);
        scheduler.schedule("0/4 * * * * ?", (Runnable) this::scan);
        scheduler.start();
    }

    private void scan() {
        List<SceneVersionDto> sceneVersionDtoList = sceneVersionAService.fetchAll();
        for (SceneVersionDto sceneVersionDto : sceneVersionDtoList) {
            Integer status = sceneVersionDto.getStatus();
            if(NumberUtil.equals(SceneConst.SCENE_VERSION_STATUS_DEBUG_DEPLOYING, status)) {

            } else if (NumberUtil.equals(SceneConst.SCENE_VERSION_STATUS_RUN_DEPLOYING, status)) {

            } else {
                continue;
            }
        }
    }

    private void snapshotDeployProgress(Long versionId) {
        EnvDto envDto = envService.fetchDebugEnv();
        List<VersionProcessDto> versionProcessDtoList =
                versionProcessAService.fetchListByVersionId(ListUtil.of(versionId), true).get(versionId);
        List<String> processIdList = versionProcessDtoList.stream().map(VersionProcessDto::getProcessId).collect(Collectors.toList());


    }

    private void releaseDeployProgress(Long versionId) {

    }

    private Map<String, ProcessDeployProgressDto> getProcessDeployStatusMap(List<String> processIdList, List<Long> envIdList) {

        if(CollUtil.isEmpty(processIdList)) return MapUtil.empty();

        Map<String, ProcessDeployProgressDto> ret = new HashMap<>();

        for (Long envId : Opt.ofNullable(envIdList).orElse(ListUtil.empty())) {
            Map<String, DeployStatusDto> deployStatusDtoMap =
                    processDeployService.fetchDeployStatus(processIdList, envId);
            Assert.isTrue(deployStatusDtoMap.size() == processIdList.size(), "资源尚未部署，无法查询状态");

            for (Map.Entry<String, DeployStatusDto> entry : deployStatusDtoMap.entrySet()) {
                String processId = entry.getKey();
                DeployStatusDto _deployStatusDto = entry.getValue();
                ProcessDeployProgressDto processDeployProgressDto =
                        ret.computeIfAbsent(processId, key -> new ProcessDeployProgressDto(1, new HashMap<>()));

                if(_deployStatusDto.getStatus() == ProcessConst.PROCESS_DEPLOY_STATUS__EXCEPTION) {
                    // 如果当前部署是失败，则不管之前是什么状态，都修改为失败
                    processDeployProgressDto.setStatus(_deployStatusDto.getStatus());

                    // 记录异常结果
                    EnvDto envDto = envService.fetchOne(envId);
                    Map<String, String> messageMap =
                            Opt.ofNullable(processDeployProgressDto.getErrorMessageMap()).orElse(new LinkedHashMap<>());
                    messageMap.put(envDto.getName(), _deployStatusDto.getMessage());
                    processDeployProgressDto.setErrorMessageMap(messageMap);
                } else if (processDeployProgressDto.getStatus() == 1) { // 默认赋值为 1
                    // 如果之前是成功部署的，则当前环境状态可以直接覆盖总状态
                    processDeployProgressDto.setStatus(_deployStatusDto.getStatus());
                }
            }
        }

        return ret;
    }


}
