package com.brandnewdata.mop.poc.bff.service.homepage;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.bff.bo.HomeSceneBo;
import com.brandnewdata.mop.poc.bff.bo.HomeSceneStatisticCountBo;
import com.brandnewdata.mop.poc.constant.SceneConst;
import com.brandnewdata.mop.poc.env.dto.EnvDto;
import com.brandnewdata.mop.poc.env.service.IEnvService;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDto;
import com.brandnewdata.mop.poc.operate.dto.ProcessInstanceStateDto;
import com.brandnewdata.mop.poc.operate.dto.filter.ProcessInstanceFilter;
import com.brandnewdata.mop.poc.operate.service.IProcessInstanceService;
import com.brandnewdata.mop.poc.process.dto.ProcessReleaseDeployDto;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService;
import com.brandnewdata.mop.poc.scene.dto.SceneDto;
import com.brandnewdata.mop.poc.scene.dto.SceneReleaseDeployDto;
import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;
import com.brandnewdata.mop.poc.scene.service.ISceneService;
import com.brandnewdata.mop.poc.scene.service.atomic.ISceneReleaseDeployAService;
import com.brandnewdata.mop.poc.scene.service.atomic.ISceneVersionAService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class HomeSceneStatisticService {

    private final IEnvService envService;

    private final ISceneService sceneService;

    private final ISceneVersionAService sceneVersionAService;

    private final IProcessDeployService processDeployService;

    private final IProcessInstanceService processInstanceService;

    private final ISceneReleaseDeployAService sceneReleaseDeployService;


    public HomeSceneStatisticService(IEnvService envService,
                                     ISceneService sceneService,
                                     ISceneVersionAService sceneVersionAService,
                                     IProcessDeployService processDeployService,
                                     IProcessInstanceService processInstanceService,
                                     ISceneReleaseDeployAService sceneReleaseDeployService) {
        this.sceneService = sceneService;
        this.sceneVersionAService = sceneVersionAService;
        this.processInstanceService = processInstanceService;
        this.envService = envService;
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

    public List<HomeSceneBo> sceneList() {
        List<SceneVersionDto> sceneVersionDtoList = sceneVersionAService.fetchAll();
        List<SceneVersionDto> filterSceneVersionList = sceneVersionDtoList.stream()
                .sorted(Comparator.comparing(SceneVersionDto::getUpdateTime))
                .filter(sceneVersionDto -> NumberUtil.equals(sceneVersionDto.getStatus(), SceneConst.SCENE_VERSION_STATUS__RUNNING))
                .limit(10).collect(Collectors.toList());
        if(CollUtil.isEmpty(filterSceneVersionList)) return ListUtil.empty();

        List<Long> versionIdList = filterSceneVersionList.stream().map(SceneVersionDto::getId).collect(Collectors.toList());
        Map<Long, List<SceneReleaseDeployDto>> sceneReleaseDeployDtoMap = sceneReleaseDeployService.fetchListByVersionId(versionIdList);

        List<HomeSceneBo> ret = new ArrayList<>();
        for (SceneVersionDto sceneVersionDto : filterSceneVersionList) {
            Long sceneId = sceneVersionDto.getSceneId();
            SceneDto sceneDto = sceneService.fetchById(ListUtil.of(sceneId)).get(sceneId);
            Long versionId = sceneVersionDto.getId();
            List<SceneReleaseDeployDto> sceneReleaseDeployDtoList = sceneReleaseDeployDtoMap.get(versionId);
            List<String> envList = new ArrayList<>();
            List<String> processInstanceCountList = new ArrayList<>();
            List<String> processInstanceFailCountList = new ArrayList<>();

            // caiwillie
            Map<Long, List<String>> envProcessMap = sceneReleaseDeployDtoList.stream()
                    .collect(Collectors.groupingBy(SceneReleaseDeployDto::getEnvId,
                            Collectors.mapping(SceneReleaseDeployDto::getProcessId, Collectors.toList())));

            for (Map.Entry<Long, List<String>> entry : envProcessMap.entrySet()) {
                Long envId = entry.getKey();
                List<String> processIdList = entry.getValue();
                assembleSceneList(envList, processInstanceCountList, processInstanceFailCountList, envId, processIdList);
            }

            String env = StrUtil.join("/", envList);
            String processInstanceCount = StrUtil.join("、", processInstanceCountList);
            String processInstanceFailCount = StrUtil.join("、", processInstanceFailCountList);

            HomeSceneBo homeSceneBo = new HomeSceneBo();
            homeSceneBo.setName(sceneDto.getName());
            homeSceneBo.setVersion(sceneVersionDto.getVersion());
            homeSceneBo.setStatus(sceneVersionDto.getStatus());
            homeSceneBo.setUpdateTime(LocalDateTimeUtil.formatNormal(sceneVersionDto.getUpdateTime()));
            homeSceneBo.setEnv(env);
            homeSceneBo.setProcessInstanceCount(processInstanceCount);
            homeSceneBo.setProcessInstanceFailCount(processInstanceFailCount);
            ret.add(homeSceneBo);
        }
        return ret;
    }

    private void assembleSceneCount(HomeSceneStatisticCountBo bo) {
        List<SceneVersionDto> sceneVersionDtoList = sceneVersionAService.fetchAll();
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

        ProcessInstanceFilter processInstanceFilter = new ProcessInstanceFilter();
        List<ListViewProcessInstanceDto> listViewProcessInstanceDtoList =
                processInstanceService.listProcessInstanceCacheByZeebeKey(envId, zeebeKeyList, processInstanceFilter);


        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        int processInstanceCount = Opt.of(bo.getProcessInstanceCount()).orElse(0);
        int processInstanceFailCount = Opt.of(bo.getProcessInstanceFailCount()).orElse(0);
        for (ListViewProcessInstanceDto listViewProcessInstanceDto : listViewProcessInstanceDtoList) {
            // 超过7日的过滤
            if(startTime.compareTo(listViewProcessInstanceDto.getStartDate()) > 0) continue;
            processInstanceCount++;
            if (listViewProcessInstanceDto.getState() == ProcessInstanceStateDto.INCIDENT) {
                processInstanceFailCount++;
            }
        }
        bo.setProcessInstanceCount(processInstanceCount);
        bo.setProcessInstanceFailCount(processInstanceFailCount);
    }

    private void assembleSceneList(List<String> envList,
                                   List<String> processInstanceCountList,
                                   List<String> processInstanceFailCountList,
                                   Long envId,
                                   List<String> processIdList) {
        EnvDto envDto = envService.fetchOne(envId);
        String envName = envDto.getName();
        envList.add(envName);

        Map<String, ProcessReleaseDeployDto> processReleaseDeployDtoMap =
                processDeployService.fetchReleaseByEnvIdAndProcessId(envId, processIdList);

        List<Long> zeebeKeyList = processReleaseDeployDtoMap.values().stream()
                .map(ProcessReleaseDeployDto::getProcessZeebeKey).collect(Collectors.toList());

        ProcessInstanceFilter processInstanceFilter = new ProcessInstanceFilter();
        List<ListViewProcessInstanceDto> listViewProcessInstanceDtoList =
                processInstanceService.listProcessInstanceCacheByZeebeKey(envId, zeebeKeyList, processInstanceFilter);

        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        int processInstanceCount = 0;
        int processInstanceFailCount = 0;
        for (ListViewProcessInstanceDto listViewProcessInstanceDto : listViewProcessInstanceDtoList) {
            if(listViewProcessInstanceDto.getStartDate().compareTo(startTime) > 0) continue;
            processInstanceCount++;
            if (listViewProcessInstanceDto.getState() == ProcessInstanceStateDto.INCIDENT) {
                processInstanceFailCount++;
            }
        }
        processInstanceCountList.add(StrUtil.format("{} ({})", processInstanceCount, envName));
        processInstanceFailCountList.add(StrUtil.format("{} ({})", processInstanceFailCount, envName));
    }


}
