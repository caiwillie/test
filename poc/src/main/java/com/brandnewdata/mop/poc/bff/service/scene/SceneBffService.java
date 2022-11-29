package com.brandnewdata.mop.poc.bff.service.scene;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.NumberUtil;
import com.brandnewdata.mop.poc.bff.converter.scene.DebugProcessInstanceVoConverter;
import com.brandnewdata.mop.poc.bff.converter.scene.SceneDtoConverter;
import com.brandnewdata.mop.poc.bff.converter.scene.SceneVersionVoConverter;
import com.brandnewdata.mop.poc.bff.converter.scene.VersionProcessDtoConverter;
import com.brandnewdata.mop.poc.bff.vo.scene.DebugProcessInstanceVo;
import com.brandnewdata.mop.poc.bff.vo.scene.SceneVersionVo;
import com.brandnewdata.mop.poc.bff.vo.scene.SceneVo;
import com.brandnewdata.mop.poc.bff.vo.scene.VersionProcessVo;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.constant.SceneConst;
import com.brandnewdata.mop.poc.env.dto.EnvDto;
import com.brandnewdata.mop.poc.env.service.IEnvService;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDto;
import com.brandnewdata.mop.poc.operate.service.IProcessInstanceService;
import com.brandnewdata.mop.poc.process.dto.ProcessSnapshotDeployDto;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService2;
import com.brandnewdata.mop.poc.scene.dto.SceneDto2;
import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;
import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;
import com.brandnewdata.mop.poc.scene.service.ISceneService2;
import com.brandnewdata.mop.poc.scene.service.ISceneVersionService;
import com.brandnewdata.mop.poc.scene.service.IVersionProcessService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SceneBffService {

    private final ISceneService2 sceneService;

    private final ISceneVersionService sceneVersionService;

    private final IVersionProcessService versionProcessService;

    private final IEnvService envService;

    private final IProcessDeployService2 processDeployService;

    private final IProcessInstanceService processInstanceService;

    public SceneBffService(ISceneService2 sceneService,
                           ISceneVersionService sceneVersionService,
                           IVersionProcessService versionProcessService,
                           IEnvService envService,
                           IProcessDeployService2 processDeployService,
                           IProcessInstanceService processInstanceService) {
        this.sceneService = sceneService;
        this.sceneVersionService = sceneVersionService;
        this.versionProcessService = versionProcessService;
        this.envService = envService;
        this.processDeployService = processDeployService;
        this.processInstanceService = processInstanceService;
    }

    public Page<SceneVo> page(Integer pageNum, Integer pageSize, String name) {
        Page<SceneDto2> page = sceneService.page(pageNum, pageSize, name);
        List<SceneDto2> records = page.getRecords();

        if(CollUtil.isEmpty(records)) return new Page<>(page.getTotal(), ListUtil.empty());
        List<SceneVo> sceneVos = new ArrayList<>();

        // 获取所有场景的最新版本
        List<Long> sceneIdList = records.stream().map(SceneDto2::getId).collect(Collectors.toList());
        Map<Long, SceneVersionDto> sceneVersionDtoMap = sceneVersionService.fetchLatestVersion(sceneIdList);
        List<Long> versionIdList = sceneVersionDtoMap.values().stream().map(SceneVersionDto::getId).collect(Collectors.toList());

        // 获取某版本下的流程个数
        Map<Long, Integer> countMap = versionProcessService.fetchVersionProcessCountByVersionIdList(versionIdList);
        // 获取某版本下的最新流程
        Map<Long, VersionProcessDto> processDtoMap = versionProcessService.fetchLatestProcessByVersionIdList(versionIdList);

        for (SceneDto2 sceneDto : records) {
            SceneVo sceneVo = new SceneVo().from(sceneDto);
            Long versionId = sceneVersionDtoMap.get(sceneVo.getId()).getId();
            Integer count = Opt.ofNullable(countMap.get(versionId)).orElse(0);
            String img = Opt.ofNullable(processDtoMap.get(versionId)).map(VersionProcessDto::getProcessImg).orElse(null);
            sceneVo.setProcessCount(count);
            sceneVo.setImgUrl(img);
            sceneVos.add(sceneVo);
        }
        return new Page<>(page.getTotal(), sceneVos);
    }

    public List<SceneVersionVo> versionList(Long sceneId) {
        Assert.notNull(sceneId, "场景id不能为空");
        List<SceneVersionDto> sceneVersionDtoList =
                sceneVersionService.fetchSceneVersionListBySceneId(ListUtil.of(sceneId)).get(sceneId);
        return sceneVersionDtoList.stream()
                .map(SceneVersionVoConverter::createFrom).collect(Collectors.toList());
    }

    public List<VersionProcessVo> processList(Long versionId) {
        Assert.notNull(versionId, "版本id不能为空");
        List<VersionProcessDto> versionProcessDtoList =
                versionProcessService.fetchVersionProcessListByVersionId(ListUtil.of(versionId), false).get(versionId);
        return Opt.ofNullable(versionProcessDtoList).orElse(ListUtil.empty()).stream()
                .map(dto -> new VersionProcessVo().from(dto)).collect(Collectors.toList());
    }

    public SceneVo save(SceneVo vo) {
        SceneDto2 ret = sceneService.save(SceneDtoConverter.createFrom(vo));
        return vo.from(ret);
    }

    public VersionProcessVo processSave(VersionProcessVo vo) {
        VersionProcessDto dto = sceneVersionService.saveProcess(VersionProcessDtoConverter.createFrom(vo));
        return new VersionProcessVo().from(dto);
    }

    public SceneVersionVo versionDebug(Long versionId) {
        Assert.notNull(versionId, "版本id不能为空");
        EnvDto debugEnv = envService.fetchDebugEnv();
        SceneVersionDto sceneVersionDto = sceneVersionService.debug(versionId, debugEnv.getId());
        return SceneVersionVoConverter.createFrom(sceneVersionDto);
    }

    public SceneVersionVo versionDeploy(Long versionId, List<Long> envIdList, String version) {
        SceneVersionDto sceneVersionDto = sceneVersionService.deploy(versionId, envIdList, version);
        return SceneVersionVoConverter.createFrom(sceneVersionDto);
    }

    public SceneVersionVo versionStop(Long versionId) {
        SceneVersionDto sceneVersionDto = sceneVersionService.stop(versionId);
        return SceneVersionVoConverter.createFrom(sceneVersionDto);
    }

    public SceneVersionVo versionResume(Long versionId, List<Long> envIdList) {
        SceneVersionDto dto = sceneVersionService.resume(versionId, envIdList);
        return SceneVersionVoConverter.createFrom(dto);
    }

    public Page<DebugProcessInstanceVo> listDebugProcessInstance(Integer pageNum, Integer pageSize, Long versionId) {
        Assert.notNull(versionId, "版本id不能为空");
        SceneVersionDto sceneVersionDto = sceneVersionService.fetchById(ListUtil.of(versionId)).get(versionId);
        Assert.notNull(sceneVersionDto, "版本不存在。version id：{}", versionId);
        Assert.isTrue(NumberUtil.equals(sceneVersionDto.getStatus(), SceneConst.SCENE_VERSION_STATUS__DEBUGGING)
                || NumberUtil.equals(sceneVersionDto.getStatus(), SceneConst.SCENE_VERSION_STATUS__CONFIGURING),
                "版本状态异常。仅支持以下状态：配置中、调试中");

        // 获取调试环境
        EnvDto debugEnv = envService.fetchDebugEnv();
        Long envId = debugEnv.getId();

        // 获取该版本下当前的流程定义
        List<VersionProcessDto> versionProcessDtoList =
                versionProcessService.fetchVersionProcessListByVersionId(ListUtil.of(versionId), true).get(versionId);
        if(CollUtil.isEmpty(versionProcessDtoList)) return new Page<>(0, ListUtil.empty());
        Map<String, VersionProcessDto> versionProcessDtoMap =
                versionProcessDtoList.stream().collect(Collectors.toMap(VersionProcessDto::getProcessId, Function.identity()));

        // 获取流程定义
        Map<String, List<ProcessSnapshotDeployDto>> snapshotDeployMap =
                processDeployService.listSnapshotByProcessIdAndEnvId(envId, ListUtil.toList(versionProcessDtoMap.keySet()));
        Map<Long, ProcessSnapshotDeployDto> processSnapshotDeployDtoMap = snapshotDeployMap.values().stream()
                .flatMap(Collection::stream).collect(Collectors.toMap(ProcessSnapshotDeployDto::getProcessZeebeKey, Function.identity()));

        // 根据流程定义去查询流程实例
        Page<ListViewProcessInstanceDto> page =
                processInstanceService.pageProcessInstanceByZeebeKey(
                        envId, ListUtil.toList(processSnapshotDeployDtoMap.keySet()), pageNum, pageSize, new HashMap<>());

        List<DebugProcessInstanceVo> vos = new ArrayList<>();
        for (ListViewProcessInstanceDto listViewProcessInstanceDto : page.getRecords()) {
            DebugProcessInstanceVo vo = DebugProcessInstanceVoConverter.createFrom(listViewProcessInstanceDto);
            Long zeebeKey = listViewProcessInstanceDto.getProcessId();
            String processId = listViewProcessInstanceDto.getBpmnProcessId();
            ProcessSnapshotDeployDto processSnapshotDeployDto = processSnapshotDeployDtoMap.get(zeebeKey);
            VersionProcessDto versionProcessDto = versionProcessDtoMap.get(processId);
            DebugProcessInstanceVoConverter.updateFrom(vo, processSnapshotDeployDto);
            DebugProcessInstanceVoConverter.updateFrom(vo, versionProcessDto);
            vos.add(vo);
        }

        return new Page<>(page.getTotal(), vos);
    }
}
