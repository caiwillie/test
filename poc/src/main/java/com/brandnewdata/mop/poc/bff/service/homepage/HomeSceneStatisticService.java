package com.brandnewdata.mop.poc.bff.service.homepage;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Opt;
import com.brandnewdata.mop.poc.bff.bo.HomeSceneStatisticCountBo;
import com.brandnewdata.mop.poc.constant.SceneConst;
import com.brandnewdata.mop.poc.env.dto.EnvDto;
import com.brandnewdata.mop.poc.env.service.IEnvService;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDto;
import com.brandnewdata.mop.poc.operate.dto.ProcessInstanceStateDto;
import com.brandnewdata.mop.poc.operate.service.IProcessInstanceService2;
import com.brandnewdata.mop.poc.process.dto.ProcessReleaseDeployDto;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService2;
import com.brandnewdata.mop.poc.scene.dto.SceneReleaseDeployDto;
import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;
import com.brandnewdata.mop.poc.scene.service.ISceneReleaseDeployService;
import com.brandnewdata.mop.poc.scene.service.ISceneVersionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class HomeSceneStatisticService {

    private final IEnvService envService;

    private final ISceneVersionService sceneVersionService;

    private final IProcessDeployService2 processDeployService;

    private final IProcessInstanceService2 processInstanceService;

    private final ISceneReleaseDeployService sceneReleaseDeployService;

    public HomeSceneStatisticService(IEnvService envService,
                                     ISceneVersionService sceneVersionService,
                                     IProcessDeployService2 processDeployService,
                                     IProcessInstanceService2 processInstanceService,
                                     ISceneReleaseDeployService sceneReleaseDeployService) {
        this.processInstanceService = processInstanceService;
        this.envService = envService;
        this.sceneVersionService = sceneVersionService;
        this.processDeployService = processDeployService;
        this.sceneReleaseDeployService = sceneReleaseDeployService;
    }

    public HomeSceneStatisticCountBo statisticCount() {
        HomeSceneStatisticCountBo ret = new HomeSceneStatisticCountBo();
        assembleSceneCount(ret);
        List<EnvDto> envDtoList = envService.fetchEnvList();
        for (EnvDto envDto : envDtoList) {
            assembleStatisticProcessInstanceCount(ret, envDto.getId());
        }
        return ret;
    }

    private void assembleSceneCount(HomeSceneStatisticCountBo bo) {
        List<SceneVersionDto> sceneVersionDtoList = sceneVersionService.fetchAll();
        int total = 0;
        int running = 0;
        for (SceneVersionDto sceneVersionDto : sceneVersionDtoList) {
            total++;
            if(sceneVersionDto.getStatus() == SceneConst.SCENE_VERSION_STATUS__RUNNING) {
                running++;
            }
        }
        bo.setSceneCount(total);
        bo.setSceneRunningCount(running);
    }

    private void assembleStatisticProcessInstanceCount(HomeSceneStatisticCountBo bo, Long envId) {
        // 获取符合过滤条件的流程id列表
        List<SceneReleaseDeployDto> sceneReleaseDeployDtoList = sceneReleaseDeployService.fetchByEnvId(envId);;
        Map<String, SceneReleaseDeployDto> sceneReleaseDeployDtoMap = sceneReleaseDeployDtoList.stream()
                .collect(Collectors.toMap(SceneReleaseDeployDto::getProcessId, Function.identity()));

        // 获取 process release deploy列表
        Map<String, ProcessReleaseDeployDto> processReleaseDeployDtoMap =
                processDeployService.fetchReleaseByEnvIdAndProcessId(envId, ListUtil.toList(sceneReleaseDeployDtoMap.keySet()));

        List<Long> zeebeKeyList = processReleaseDeployDtoMap.values().stream()
                .map(ProcessReleaseDeployDto::getProcessZeebeKey).collect(Collectors.toList());

        List<ListViewProcessInstanceDto> listViewProcessInstanceDtoList =
                processInstanceService.listProcessInstanceCacheByZeebeKey(envId, zeebeKeyList);


        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        int processInstanceCount = Opt.of(bo.getProcessInstanceCount()).orElse(0);
        int processInstanceFailCount = Opt.of(bo.getProcessInstanceFailCount()).orElse(0);
        for (ListViewProcessInstanceDto listViewProcessInstanceDto : listViewProcessInstanceDtoList) {
            if(listViewProcessInstanceDto.getStartDate().compareTo(startTime) > 0) continue;
            processInstanceCount++;
            if (listViewProcessInstanceDto.getState() == ProcessInstanceStateDto.INCIDENT) {
                processInstanceFailCount++;
            }
        }
        bo.setProcessInstanceCount(processInstanceCount);
        bo.setProcessInstanceFailCount(processInstanceFailCount);
    }
}
