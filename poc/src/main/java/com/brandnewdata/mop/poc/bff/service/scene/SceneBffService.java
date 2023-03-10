package com.brandnewdata.mop.poc.bff.service.scene;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.brandnewdata.mop.poc.bff.converter.process.ProcessDefinitionVoConverter;
import com.brandnewdata.mop.poc.bff.converter.scene.*;
import com.brandnewdata.mop.poc.bff.converter.scene.external.ConnectorConfigVoConverter;
import com.brandnewdata.mop.poc.bff.vo.process.ProcessDefinitionVo;
import com.brandnewdata.mop.poc.bff.vo.scene.DebugProcessInstanceVo;
import com.brandnewdata.mop.poc.bff.vo.scene.SceneVersionVo;
import com.brandnewdata.mop.poc.bff.vo.scene.SceneVo;
import com.brandnewdata.mop.poc.bff.vo.scene.VersionProcessVo;
import com.brandnewdata.mop.poc.bff.vo.scene.external.ConnectorConfigVo;
import com.brandnewdata.mop.poc.bff.vo.scene.external.ExportQueryVo;
import com.brandnewdata.mop.poc.bff.vo.scene.external.PrepareLoadVo;
import com.brandnewdata.mop.poc.bff.vo.scene.operate.SceneDeployVo;
import com.brandnewdata.mop.poc.bff.vo.scene.operate.SceneVersionDeployVo;
import com.brandnewdata.mop.poc.bff.vo.scene.operate.VersionProcessDeployVo;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.constant.SceneConst;
import com.brandnewdata.mop.poc.env.dto.EnvDto;
import com.brandnewdata.mop.poc.env.service.IEnvService;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDto;
import com.brandnewdata.mop.poc.operate.dto.filter.ProcessInstanceFilter;
import com.brandnewdata.mop.poc.operate.service.IProcessInstanceService;
import com.brandnewdata.mop.poc.process.dto.ProcessSnapshotDeployDto;
import com.brandnewdata.mop.poc.process.manager.ConnectorManager;
import com.brandnewdata.mop.poc.process.manager.dto.ConnectorBasicInfo;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService;
import com.brandnewdata.mop.poc.scene.dto.*;
import com.brandnewdata.mop.poc.scene.dto.external.ConfirmLoadDto;
import com.brandnewdata.mop.poc.scene.dto.external.ConnectorConfigDto;
import com.brandnewdata.mop.poc.scene.dto.external.PrepareLoadDto;
import com.brandnewdata.mop.poc.scene.service.IDataExternalCService;
import com.brandnewdata.mop.poc.scene.service.ISceneService;
import com.brandnewdata.mop.poc.scene.service.atomic.ISceneReleaseDeployAService;
import com.brandnewdata.mop.poc.scene.service.atomic.ISceneVersionAService;
import com.brandnewdata.mop.poc.scene.service.atomic.IVersionProcessAService;
import com.brandnewdata.mop.poc.scene.service.combine.ISceneVersionCService;
import com.brandnewdata.mop.poc.scene.service.combine.IVersionProcessCService;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SceneBffService {

    private final ISceneService sceneService;

    private final ISceneVersionCService sceneVersionCService;

    private final ISceneVersionAService sceneVersionAService;

    private final IVersionProcessAService versionProcessAService;

    private final IVersionProcessCService versionProcessCService;

    private final IEnvService envService;

    private final IProcessDeployService processDeployService;

    private final IProcessInstanceService processInstanceService;

    private final ISceneReleaseDeployAService sceneReleaseDeployService;

    private final IDataExternalCService dataExternalService;

    private final ConnectorManager connectorManager;

    public SceneBffService(ISceneService sceneService,
                           ISceneVersionCService sceneVersionCService,
                           ISceneVersionAService sceneVersionAService,
                           IVersionProcessAService versionProcessAService,
                           IVersionProcessCService versionProcessCService,
                           IEnvService envService,
                           IProcessDeployService processDeployService,
                           IProcessInstanceService processInstanceService,
                           ISceneReleaseDeployAService sceneReleaseDeployService,
                           IDataExternalCService dataExternalService,
                           ConnectorManager connectorManager) {
        this.sceneService = sceneService;
        this.sceneVersionCService = sceneVersionCService;
        this.sceneVersionAService = sceneVersionAService;
        this.versionProcessAService = versionProcessAService;
        this.versionProcessCService = versionProcessCService;
        this.envService = envService;
        this.processDeployService = processDeployService;
        this.processInstanceService = processInstanceService;
        this.sceneReleaseDeployService = sceneReleaseDeployService;
        this.dataExternalService = dataExternalService;
        this.connectorManager = connectorManager;
    }

    public Page<SceneVo> page(Long projectId, Integer pageNum, Integer pageSize, String name) {
        Page<SceneDto> page = sceneService.page(projectId, pageNum, pageSize, name);
        List<SceneDto> records = page.getRecords();

        if(CollUtil.isEmpty(records)) return new Page<>(page.getTotal(), ListUtil.empty());
        List<SceneVo> sceneVos = new ArrayList<>();

        // ?????????????????????????????????
        List<Long> sceneIdList = records.stream().map(SceneDto::getId).collect(Collectors.toList());
        Map<Long, SceneVersionDto> sceneVersionDtoMap = sceneVersionAService.fetchLatestOneBySceneId(sceneIdList, null);
        List<Long> versionIdList = sceneVersionDtoMap.values().stream().map(SceneVersionDto::getId).collect(Collectors.toList());

        // ?????????????????????????????????
        Map<Long, Integer> countMap = versionProcessAService.fetchCountByVersionId(versionIdList);
        // ?????????????????????????????????
        Map<Long, VersionProcessDto> processDtoMap = versionProcessAService.fetchLatestOneByVersionId(versionIdList);

        for (SceneDto sceneDto : records) {
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
        Assert.notNull(sceneId, "??????id????????????");
        List<SceneVersionDto> sceneVersionDtoList =
                sceneVersionAService.fetchListBySceneId(ListUtil.of(sceneId)).get(sceneId);

        // ??????????????????
        List<Long> versionIdList = sceneVersionDtoList.stream().map(SceneVersionDto::getId).collect(Collectors.toList());
        Map<Long, List<SceneReleaseDeployDto>> sceneReleaseDeployDtoListMap =
                sceneReleaseDeployService.fetchListByVersionId(versionIdList);

        Map<Long, List<EnvDto>> envDtoListMap = new HashMap<>();
        for (SceneVersionDto sceneVersionDto : sceneVersionDtoList) {
            List<SceneReleaseDeployDto> sceneReleaseDeployDtoList = sceneReleaseDeployDtoListMap.get(sceneVersionDto.getId());
            if(sceneReleaseDeployDtoList == null) continue;
            List<EnvDto> envDtoList = sceneReleaseDeployDtoList.stream().map(sceneReleaseDeployDto -> {
                EnvDto envDto = new EnvDto();
                envDto.setId(sceneReleaseDeployDto.getEnvId());
                return envDto;
            }).collect(Collectors.toList());
            envDtoListMap.put(sceneVersionDto.getId(), envDtoList);
        }

        return sceneVersionDtoList.stream()
                .map(sceneVersionDto ->
                        SceneVersionVoConverter.createFrom(sceneVersionDto, envDtoListMap.get(sceneVersionDto.getId())))
                .collect(Collectors.toList());
    }

    public SceneVersionVo versionDetail(Long versionId) {
        SceneVersionDto sceneVersionDto = sceneVersionAService.fetchById(ListUtil.of(versionId)).get(versionId);

        List<SceneReleaseDeployDto> sceneReleaseDeployDtoList =
                Opt.ofNullable(sceneReleaseDeployService.fetchListByVersionId(ListUtil.of(versionId)).get(versionId))
                        .orElse(ListUtil.empty());

        List<EnvDto> envDtoList = sceneReleaseDeployDtoList.stream().map(sceneReleaseDeployDto -> {
            EnvDto envDto = new EnvDto();
            envDto.setId(sceneReleaseDeployDto.getEnvId());
            return envDto;
        }).collect(Collectors.toList());

        return SceneVersionVoConverter.createFrom(sceneVersionDto, envDtoList);
    }

    public List<VersionProcessVo> processList(Long versionId) {
        Assert.notNull(versionId, "??????id????????????");
        List<VersionProcessDto> versionProcessDtoList =
                versionProcessAService.fetchListByVersionId(ListUtil.of(versionId), false).get(versionId);
        return Opt.ofNullable(versionProcessDtoList).orElse(ListUtil.empty()).stream()
                .map(VersionProcessVoConverter::createFrom).collect(Collectors.toList());
    }

    public SceneVo save(SceneVo vo) {
        SceneDto ret = sceneService.save(SceneDtoConverter.createFrom(vo));
        return vo.from(ret);
    }

    public void delete(Long sceneId) {
        sceneService.deleteById(sceneId);
    }

    public void export(ExportQueryVo exportQueryVo, HttpServletResponse response) {
        SceneVersionExportDto exportDto = dataExternalService.export(exportQueryVo.getVersionId(), exportQueryVo.getProcessIdList());
        // ?????????????????????????????? Access-Control-Expose-Headers ??????????????????????????????
        response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
        response.setCharacterEncoding("UTF-8");
        ServletUtil.write(response, new ByteArrayInputStream(exportDto.getBytes()), "application/zip", exportDto.getFileName());
    }

    public PrepareLoadVo prepareLoad(byte[] bytes) {
        PrepareLoadVo ret = new PrepareLoadVo();
        PrepareLoadDto prepareLoadDto = dataExternalService.prepareLoad(bytes);
        ret.setId(String.valueOf(prepareLoadDto.getId()));
        List<ConnectorConfigDto> configs = prepareLoadDto.getConfigs();
        List<ConnectorConfigVo> configVoList = configs.stream().map(ConnectorConfigVoConverter::createFrom).collect(Collectors.toList());
        for (ConnectorConfigVo connectorConfigVo : configVoList) {
            ConnectorBasicInfo connectorBasicInfo = connectorManager.getConnectorBasicInfo(connectorConfigVo.getConnectorGroup(),
                    connectorConfigVo.getConnectorId(), connectorConfigVo.getConnectorVersion());
            if(connectorBasicInfo == null) continue;
            connectorConfigVo.setConnectorType(connectorBasicInfo.getConnectorType());
        }
        ret.setConfigureList(configVoList);
        return ret;
    }

    public List<ConnectorConfigVo> listConnectorConfig(String connectorGroup, String connectorId, String connectorVersion) {
        List<ConnectorConfigDto> configs = dataExternalService.fetchConnectorConfigList(connectorGroup, connectorId, connectorVersion);
        return configs.stream().map(ConnectorConfigVoConverter::createFrom).collect(Collectors.toList());
    }

    public SceneVersionVo confirmLoad(PrepareLoadVo vo) {
        String id = vo.getId();
        String sceneName = vo.getSceneName();
        String projectId = vo.getProjectId();
        Map<String, String> configMap = new HashMap<>();
        for (ConnectorConfigVo connectorConfigVo : Opt.ofNullable(vo.getConfigureList()).orElse(ListUtil.empty())) {
            String configureId = connectorConfigVo.getConfigureId();
            String newConfigureId = connectorConfigVo.getNewConfigureId();

            Assert.notNull(configureId);
            Assert.notNull(newConfigureId);
            configMap.put(configureId, newConfigureId);
        }

        ConfirmLoadDto confirmLoadDto = new ConfirmLoadDto();
        confirmLoadDto.setProjectId(Opt.ofNullable(projectId).map(Long::valueOf).orElse(null));
        confirmLoadDto.setId(Opt.of(Long.parseLong(id)).map(Long::valueOf).orElse(null));
        confirmLoadDto.setNewSceneName(sceneName);
        confirmLoadDto.setConfigMap(configMap);
        SceneVersionDto sceneVersionDto = dataExternalService.confirmLoad(confirmLoadDto);
        return SceneVersionVoConverter.createFrom(sceneVersionDto);
    }

    public VersionProcessVo processSave(VersionProcessVo vo) {
        VersionProcessDto dto = versionProcessCService.save(VersionProcessDtoConverter.createFrom(vo));
        return VersionProcessVoConverter.createFrom(dto);
    }

    public void processDelete(Long id) {
        versionProcessCService.deleteById(id);
    }

    public void processDebug(VersionProcessVo vo) {
        versionProcessCService.debug(VersionProcessDtoConverter.createFrom(vo), vo.getVariables());
    }

    public SceneVersionVo debugVersion(Long versionId) {
        Assert.notNull(versionId, "??????id????????????");
        EnvDto debugEnv = envService.fetchDebugEnv();
        SceneVersionDto sceneVersionDto = sceneVersionCService.debug(versionId, debugEnv.getId());
        return SceneVersionVoConverter.createFrom(sceneVersionDto);
    }

    public SceneVersionVo stopDebugVersion(Long versionId) {
        Assert.notNull(versionId, "??????id????????????");
        EnvDto debugEnv = envService.fetchDebugEnv();
        SceneVersionDto sceneVersionDto = sceneVersionCService.stopDebug(versionId, debugEnv.getId());
        return SceneVersionVoConverter.createFrom(sceneVersionDto);
    }

    public boolean checkNewReleaseName(Long sceneId, String releaseName) {
        return sceneVersionAService.checkNewReleaseVersion(sceneId, releaseName);
    }

    public SceneVersionVo deployVersion(Long versionId, List<Long> envIdList, String version) {
        // ?????? scene name ?????????????????????
        Long sceneId = sceneVersionAService.fetchById(ListUtil.of(versionId)).get(versionId).getSceneId();
        String sceneName = sceneService.fetchById(ListUtil.of(sceneId)).get(sceneId).getName();
        SceneVersionDto sceneVersionDto = sceneVersionCService.deploy(versionId, sceneName, envIdList, version);
        return SceneVersionVoConverter.createFrom(sceneVersionDto);
    }

    public SceneVersionVo versionStop(Long versionId) {
        SceneVersionDto sceneVersionDto = sceneVersionCService.stop(versionId);
        return SceneVersionVoConverter.createFrom(sceneVersionDto);
    }

    public SceneVersionVo versionResume(Long versionId, List<Long> envIdList) {
        SceneVersionDto dto = sceneVersionCService.resume(versionId, envIdList);
        return SceneVersionVoConverter.createFrom(dto);
    }

    public SceneVersionVo versionCopyToNew(SceneVersionVo oldSceneVersionVo) {
        SceneVersionDto sceneVersionDto = sceneVersionCService.copyToNew(oldSceneVersionVo.getId());
        return SceneVersionVoConverter.createFrom(sceneVersionDto);
    }

    public void versionDelete(Long id) {
        sceneVersionCService.delete(id);
    }

    public Page<DebugProcessInstanceVo> listDebugProcessInstance(Integer pageNum, Integer pageSize, Long versionId) {
        Assert.notNull(versionId, "??????id????????????");
        SceneVersionDto sceneVersionDto = sceneVersionAService.fetchById(ListUtil.of(versionId)).get(versionId);
        Assert.notNull(sceneVersionDto, "??????????????????version id???{}", versionId);
        Assert.isTrue(NumberUtil.equals(sceneVersionDto.getStatus(), SceneConst.SCENE_VERSION_STATUS__DEBUGGING)
                || NumberUtil.equals(sceneVersionDto.getStatus(), SceneConst.SCENE_VERSION_STATUS__CONFIGURING),
                "??????????????????????????????????????????????????????????????????");

        // ??????????????????
        EnvDto debugEnv = envService.fetchDebugEnv();
        Long envId = debugEnv.getId();

        // ???????????????????????????????????????
        List<VersionProcessDto> versionProcessDtoList =
                versionProcessAService.fetchListByVersionId(ListUtil.of(versionId), true).get(versionId);
        if(CollUtil.isEmpty(versionProcessDtoList)) return new Page<>(0, ListUtil.empty());
        Map<String, VersionProcessDto> versionProcessDtoMap =
                versionProcessDtoList.stream().collect(Collectors.toMap(VersionProcessDto::getProcessId, Function.identity()));

        // ??????????????????
        Map<String, List<ProcessSnapshotDeployDto>> snapshotDeployMap =
                processDeployService.listSnapshotByEnvIdAndProcessId(envId, ListUtil.toList(versionProcessDtoMap.keySet()));
        Map<Long, ProcessSnapshotDeployDto> processSnapshotDeployDtoMap = snapshotDeployMap.values().stream()
                .flatMap(Collection::stream).collect(Collectors.toMap(ProcessSnapshotDeployDto::getProcessZeebeKey,
                        Function.identity(), (a, b) -> b));

        // ???????????????????????????????????????
        ProcessInstanceFilter processInstanceFilter = new ProcessInstanceFilter();
        Page<ListViewProcessInstanceDto> page = processInstanceService.pageProcessInstanceByZeebeKey(
                envId, ListUtil.toList(processSnapshotDeployDtoMap.keySet()), pageNum, pageSize, processInstanceFilter, new HashMap<>());

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

    public ProcessDefinitionVo definitionDebugProcessInstance(DebugProcessInstanceVo vo) {
        String processName = vo.getProcessName();

        // ????????????
        Long snapshotDeployId = vo.getSnapshotDeployId();
        Assert.notNull(snapshotDeployId, "??????id????????????");
        ProcessSnapshotDeployDto processSnapshotDeployDto =
                processDeployService.listSnapshotById(ListUtil.of(snapshotDeployId)).get(snapshotDeployId);
        Assert.notNull(processSnapshotDeployDto, "??????id?????????: {}", snapshotDeployId);

        // ??????????????????
        return ProcessDefinitionVoConverter.createFrom(processSnapshotDeployDto.getProcessId(),
                processName, processSnapshotDeployDto.getProcessXml());

    }

    public List<SceneDeployVo> listSceneDeploy(Long envId) {
        Assert.notNull(envId, "??????id????????????");
        List<SceneReleaseDeployDto> sceneReleaseDeployDtoList = sceneReleaseDeployService.fetchByEnvId(envId);

        CollUtil.sort(sceneReleaseDeployDtoList, (o1, o2) -> {
            LocalDateTime time1 = o1.getUpdateTime();
            LocalDateTime time2 = o2.getUpdateTime();

            // ????????????????????????
            int result = time2.compareTo(time1);
            if(result != 0) return result;

            // ???????????????id
            return o2.getId().compareTo(o1.getId());
        });

        //
        Map<Long, SceneDeployVo> sceneDeployVoMap = new LinkedHashMap<>();
        Map<Long, LinkedHashMap<Long, SceneVersionDeployVo>> sceneVersionDeployVoMapMap = new HashMap<>();
        Map<Long, LinkedHashMap<String, VersionProcessDeployVo>> versionProcessDeployVoMapMap = new HashMap<>();
        for (SceneReleaseDeployDto sceneReleaseDeployDto : sceneReleaseDeployDtoList) {
            Long sceneId = sceneReleaseDeployDto.getSceneId();
            String sceneName = sceneReleaseDeployDto.getSceneName();
            Long versionId = sceneReleaseDeployDto.getVersionId();
            String versionName = sceneReleaseDeployDto.getVersionName();
            String processId = sceneReleaseDeployDto.getProcessId();
            String processName = sceneReleaseDeployDto.getProcessName();

            if(!sceneDeployVoMap.containsKey(sceneId)) {
                SceneDeployVo sceneDeployVo = new SceneDeployVo();
                sceneDeployVo.setSceneId(sceneId);
                sceneDeployVo.setSceneName(sceneName);
                sceneDeployVoMap.put(sceneId, sceneDeployVo);
            }

            Map<Long, SceneVersionDeployVo> sceneVersionDeployVoMap =
                    sceneVersionDeployVoMapMap.computeIfAbsent(sceneId, key -> new LinkedHashMap<>());

            if(!sceneVersionDeployVoMap.containsKey(versionId)) {
                SceneVersionDeployVo sceneVersionDeployVo = new SceneVersionDeployVo();
                sceneVersionDeployVo.setVersionId(versionId);
                sceneVersionDeployVo.setVersionName(versionName);
                sceneVersionDeployVoMap.put(versionId, sceneVersionDeployVo);
            }

            Map<String, VersionProcessDeployVo> versionProcessDeployVoMap =
                    versionProcessDeployVoMapMap.computeIfAbsent(versionId, key -> new LinkedHashMap<>());

            if(!versionProcessDeployVoMap.containsKey(processId)) {
                VersionProcessDeployVo versionProcessDeployVo = new VersionProcessDeployVo();
                versionProcessDeployVo.setProcessId(processId);
                versionProcessDeployVo.setProcessName(processName);
                versionProcessDeployVoMap.put(processId, versionProcessDeployVo);
            }
        }

        // ????????????????????????
        for (LinkedHashMap<Long, SceneVersionDeployVo> sceneVersionDeployVoMap : sceneVersionDeployVoMapMap.values()) {
            for (SceneVersionDeployVo sceneVersionDeployVo : sceneVersionDeployVoMap.values()) {
                Long versionId = sceneVersionDeployVo.getVersionId();
                List<VersionProcessDeployVo> versionProcessDeployVoList =
                        ListUtil.toList(versionProcessDeployVoMapMap.get(versionId).values());
                sceneVersionDeployVo.setProcessList(versionProcessDeployVoList);
            }
        }

        for (SceneDeployVo sceneDeployVo : sceneDeployVoMap.values()) {
            List<SceneVersionDeployVo> sceneVersionDeployVoList =
                    ListUtil.toList(sceneVersionDeployVoMapMap.get(sceneDeployVo.getSceneId()).values());
            sceneDeployVo.setVersionList(sceneVersionDeployVoList);
        }

        return ListUtil.toList(sceneDeployVoMap.values());
    }
}
