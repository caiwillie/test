package com.brandnewdata.mop.poc.scene.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.constant.ProcessConst;
import com.brandnewdata.mop.poc.constant.SceneConst;
import com.brandnewdata.mop.poc.env.dto.EnvDto;
import com.brandnewdata.mop.poc.env.service.IEnvService;
import com.brandnewdata.mop.poc.process.dto.BpmnXmlDto;
import com.brandnewdata.mop.poc.process.service.IProcessDefinitionService2;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService2;
import com.brandnewdata.mop.poc.scene.bo.SceneReleaseVersionBo;
import com.brandnewdata.mop.poc.scene.converter.SceneReleaseDeployDtoConverter;
import com.brandnewdata.mop.poc.scene.converter.SceneVersionDtoConverter;
import com.brandnewdata.mop.poc.scene.converter.SceneVersionPoConverter;
import com.brandnewdata.mop.poc.scene.converter.VersionProcessDtoConverter;
import com.brandnewdata.mop.poc.scene.dao.SceneVersionDao;
import com.brandnewdata.mop.poc.scene.dto.SceneReleaseDeployDto;
import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;
import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;
import com.brandnewdata.mop.poc.scene.po.SceneVersionPo;
import com.brandnewdata.mop.poc.util.CollectorsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SceneVersionService implements ISceneVersionService {

    @Resource
    private SceneVersionDao sceneVersionDao;

    private final IEnvService envService;

    private final IVersionProcessService versionProcessService;

    private final ISceneReleaseDeployService sceneReleaseDeployService;

    private final IProcessDeployService2 processDeployService;

    private final IProcessDefinitionService2 processDefinitionService;


    public SceneVersionService(IEnvService envService,
                               IVersionProcessService versionProcessService,
                               IProcessDeployService2 processDeployService,
                               ISceneReleaseDeployService sceneReleaseDeployService,
                               IProcessDefinitionService2 processDefinitionService) {
        this.envService = envService;
        this.versionProcessService = versionProcessService;
        this.processDeployService = processDeployService;
        this.sceneReleaseDeployService = sceneReleaseDeployService;
        this.processDefinitionService = processDefinitionService;
    }

    @Override
    public Map<Long, SceneVersionDto> fetchLatestOneBySceneId(List<Long> sceneIdList, List<Integer> statusList) {
        if (CollUtil.isEmpty(sceneIdList)) return MapUtil.empty();
        Map<Long, List<SceneVersionDto>> sceneVersionListMap = fetchSceneVersionListBySceneId(sceneIdList);
        Map<Long, SceneVersionDto> ret = new HashMap<>();

        for (Long sceneId : sceneIdList) {
            List<SceneVersionDto> sceneVersionDtoList = sceneVersionListMap.get(sceneId);
            if (CollUtil.isEmpty(sceneVersionDtoList)) {
                log.error("场景版本数据异常。sceneId：{}", sceneId);
                throw new RuntimeException("【01】场景版本数据异常");
            }
            // 选择状态不是“已停止”的最新版本; 如果状态都是“已停止”，选择最新版本
            Optional<SceneVersionDto> versionOpt = sceneVersionDtoList.stream().filter(sceneVersionDto -> {
                        if (CollUtil.isEmpty(statusList)) return true;
                        else return statusList.contains(sceneVersionDto.getStatus());
                    })
                    .findFirst();
            ret.put(sceneId, versionOpt.orElse(null));
        }
        return ret;
    }

    @Override
    public Map<Long, List<SceneVersionDto>> fetchSceneVersionListBySceneId(List<Long> sceneIdList) {
        if (CollUtil.isEmpty(sceneIdList)) return MapUtil.empty();
        long count = sceneIdList.stream().filter(Objects::isNull).count();
        Assert.isTrue(count == 0, "场景id列表不能存在空值");
        QueryWrapper<SceneVersionPo> query = new QueryWrapper<>();
        query.in(SceneVersionPo.SCENE_ID, sceneIdList);
        List<SceneVersionPo> sceneVersionPos = sceneVersionDao.selectList(query);
        return sceneVersionPos.stream().collect(Collectors.groupingBy(SceneVersionPo::getSceneId,
                Collectors.mapping(SceneVersionDtoConverter::from,
                        CollectorsUtil.toSortedList((o1, o2) -> o2.getUpdateTime().compareTo(o1.getUpdateTime())))));
    }

    @Override
    public SceneVersionDto save(SceneVersionDto sceneVersionDto) {
        SceneVersionPo sceneVersionPo = SceneVersionPoConverter.createFrom(sceneVersionDto);
        sceneVersionDao.insert(sceneVersionPo);
        return SceneVersionDtoConverter.from(sceneVersionPo);
    }

    @Override
    public VersionProcessDto saveProcess(VersionProcessDto dto) {
        Long versionId = dto.getVersionId();
        getAndCheckStatus(versionId, new int[]{SceneConst.SCENE_VERSION_STATUS__CONFIGURING});
        return versionProcessService.save(dto);
    }

    @Override
    public void deleteProcess(VersionProcessDto dto) {
        Long versionId = dto.getVersionId();
        Assert.notNull(versionId, "版本id不能为空");

        SceneVersionDto versionDto = fetchById(ListUtil.of(versionId)).get(versionId);
        Assert.notNull(versionDto, "版本id不存在。id: {}", versionId);


    }

    @Override
    public void processDebug(VersionProcessDto dto, Map<String, Object> variables) {
        Long versionId = dto.getVersionId();
        Assert.notNull(versionId, "流程版本id不能为空");
        SceneVersionDto sceneVersionDto = fetchById(ListUtil.of(versionId)).get(versionId);
        Assert.notNull(sceneVersionDto, "版本不存在。version id：{}", versionId);
        Assert.isTrue(NumberUtil.equals(sceneVersionDto.getStatus(), SceneConst.SCENE_VERSION_STATUS__DEBUGGING)
                        || NumberUtil.equals(sceneVersionDto.getStatus(), SceneConst.SCENE_VERSION_STATUS__CONFIGURING),
                "版本状态异常。仅支持以下状态：调试中");

        // 查询流程
        Long id = dto.getId();
        Assert.notNull(id, "流程不能为空");
        VersionProcessDto versionProcessDto = versionProcessService.fetchOneById(ListUtil.of(id)).get(id);
        Assert.notNull(versionProcessDto, "流程不存在: {}", id);

        // 查询调试环境
        EnvDto envDto = envService.fetchDebugEnv();
        Assert.notNull(envDto, "调试环境不存在");
        Long envId = envDto.getId();

        processDeployService.startAsync(versionProcessDto.getProcessId(), Opt.ofNullable(variables).orElse(MapUtil.empty()), envId);
    }

    @Override
    public SceneVersionDto debug(Long id, Long envId) {
        Assert.notNull(id, "版本id不能为空");
        SceneVersionDto sceneVersionDto = fetchById(ListUtil.of(id)).get(id);
        Assert.notNull(sceneVersionDto, "版本不存在。version id：{}", id);

        // 获取版本下的流程
        List<VersionProcessDto> versionProcessDtoList =
                versionProcessService.fetchListByVersionId(ListUtil.of(id), false).get(id);
        Assert.isTrue(CollUtil.isNotEmpty(versionProcessDtoList), "该版本下至少需要配置一个流程");

        Assert.notNull(envId, "环境id不能为空");

        for (VersionProcessDto versionProcessDto : versionProcessDtoList) {
            BpmnXmlDto deployDto = new BpmnXmlDto();
            deployDto.setProcessId(versionProcessDto.getProcessId());
            deployDto.setProcessName(versionProcessDto.getProcessName());
            deployDto.setProcessXml(versionProcessDto.getProcessXml());
            processDeployService.snapshotDeploy(deployDto, envId, ProcessConst.PROCESS_BIZ_TYPE__SCENE);
        }

        // 修改状态
        sceneVersionDto.setStatus(SceneConst.SCENE_VERSION_STATUS__DEBUGGING);
        sceneVersionDao.updateById(SceneVersionPoConverter.createFrom(sceneVersionDto));
        return sceneVersionDto;
    }

    @Override
    public SceneVersionDto stopDebug(Long id, Long envId) {
        SceneVersionDto sceneVersionDto = getAndCheckStatus(id, new int[]{SceneConst.SCENE_VERSION_STATUS__DEBUGGING});

        // 修改状态
        sceneVersionDto.setStatus(SceneConst.SCENE_VERSION_STATUS__CONFIGURING);
        sceneVersionDao.updateById(SceneVersionPoConverter.createFrom(sceneVersionDto));
        return sceneVersionDto;
    }

    @Override
    public SceneVersionDto stop(Long id) {
        SceneVersionDto sceneVersionDto = getAndCheckStatus(id, new int[]{SceneConst.SCENE_VERSION_STATUS__RUNNING});

        // 修改状态
        sceneVersionDto.setStatus(SceneConst.SCENE_VERSION_STATUS__STOPPED);
        sceneVersionDao.updateById(SceneVersionPoConverter.createFrom(sceneVersionDto));
        return sceneVersionDto;
    }

    @Override
    public SceneVersionDto resume(Long id, List<Long> envIdList) {
        SceneVersionDto sceneVersionDto = getAndCheckStatus(id, new int[]{SceneConst.SCENE_VERSION_STATUS__STOPPED});
        Assert.notEmpty(envIdList, "环境列表不能为空");

        // 修改状态
        sceneVersionDto.setStatus(SceneConst.SCENE_VERSION_STATUS__RUNNING);
        sceneVersionDao.updateById(SceneVersionPoConverter.createFrom(sceneVersionDto));
        return sceneVersionDto;
    }

    @Override
    public SceneVersionDto deploy(Long id, String sceneName, List<Long> envIdList, String version) {
        // 配置中，和调试中均可以进行发布
        SceneVersionDto sceneVersionDto = getAndCheckStatus(id,
                new int[]{SceneConst.SCENE_VERSION_STATUS__CONFIGURING, SceneConst.SCENE_VERSION_STATUS__DEBUGGING});
        Assert.notEmpty(envIdList, "环境列表不能为空");
        Assert.notNull(version, "版本不能为空");
        Assert.isTrue(checkNewReleaseVersion(sceneVersionDto.getSceneId(), version), "新版本号必须大于旧版本号");
        sceneVersionDto.setVersion(version);

        // 查询版本下的流程
        List<VersionProcessDto> versionProcessDtoList =
                versionProcessService.fetchListByVersionId(ListUtil.of(id), false).get(id);
        Assert.isTrue(CollUtil.isNotEmpty(versionProcessDtoList), "该版本下至少需要配置一个流程");

        // 发布到zeebe
        for (VersionProcessDto versionProcessDto : versionProcessDtoList) {
            BpmnXmlDto deployDto = new BpmnXmlDto();
            deployDto.setProcessId(versionProcessDto.getProcessId());
            deployDto.setProcessName(versionProcessDto.getProcessName());
            deployDto.setProcessXml(versionProcessDto.getProcessXml());
            processDeployService.releaseDeploy(deployDto, envIdList, ProcessConst.PROCESS_BIZ_TYPE__SCENE);
        }

        //todo caiwillie 上报保存配置

        // 保存到场景列表
        for (VersionProcessDto versionProcessDto : versionProcessDtoList) {
            for (Long envId : envIdList) {
                SceneReleaseDeployDto sceneReleaseDeployDto = new SceneReleaseDeployDto();
                SceneReleaseDeployDtoConverter.updateFrom(sceneReleaseDeployDto, versionProcessDto);
                SceneReleaseDeployDtoConverter.updateFrom(sceneReleaseDeployDto, sceneVersionDto);
                sceneReleaseDeployDto.setSceneName(sceneName);
                sceneReleaseDeployDto.setEnvId(envId);
                sceneReleaseDeployService.save(sceneReleaseDeployDto);
            }
        }

        // 更新状态
        sceneVersionDto.setStatus(SceneConst.SCENE_VERSION_STATUS__RUNNING);
        sceneVersionDao.updateById(SceneVersionPoConverter.createFrom(sceneVersionDto));
        return sceneVersionDto;
    }

    @Override
    public Map<Long, Long> countById(List<Long> idList) {
        if (CollUtil.isEmpty(idList)) return MapUtil.empty();
        Assert.isTrue(idList.stream().filter(Objects::isNull).count() == 0, "版本id不能为空");
        QueryWrapper<SceneVersionPo> query = new QueryWrapper<>();
        query.in(SceneVersionPo.ID, idList);
        query.select(SceneVersionPo.ID, "count(*) as num");
        query.groupBy(SceneVersionPo.ID);
        List<Map<String, Object>> maps = sceneVersionDao.selectMaps(query);
        Map<Long, Long> countMap = maps.stream().collect(
                Collectors.toMap(map -> (Long) map.get(SceneVersionPo.ID), map -> (Long) map.get("num")));

        // 将未找到的scene id的个数设置为0
        return idList.stream().collect(
                Collectors.toMap(Function.identity(), key -> Opt.ofNullable(countMap.get(key)).orElse(0L)));
    }

    @Override
    public Map<Long, SceneVersionDto> fetchById(List<Long> idList) {
        if (CollUtil.isEmpty(idList)) return MapUtil.empty();
        Assert.isTrue(idList.stream().filter(Objects::isNull).count() == 0, "版本id不能为空");
        QueryWrapper<SceneVersionPo> query = new QueryWrapper<>();
        query.in(SceneVersionPo.ID, idList);
        List<SceneVersionPo> sceneVersionPos = sceneVersionDao.selectList(query);
        return sceneVersionPos.stream().map(SceneVersionDtoConverter::from)
                .collect(Collectors.toMap(SceneVersionDto::getId, Function.identity()));
    }

    @Override
    @Transactional
    public SceneVersionDto copyToNew(Long id) {
        SceneVersionDto oldSceneVersionDto = getAndCheckStatus(id, new int[]{SceneConst.SCENE_VERSION_STATUS__RUNNING,
                SceneConst.SCENE_VERSION_STATUS__STOPPED});

        Long sceneId = oldSceneVersionDto.getSceneId();

        // check latest version status
        SceneVersionDto latestSceneVersionDto = fetchLatestOneBySceneId(ListUtil.of(sceneId), null).get(sceneId);
        if (NumberUtil.equals(latestSceneVersionDto.getStatus(), SceneConst.SCENE_VERSION_STATUS__DEBUGGING)) {
            throw new RuntimeException("请先退出调试模式");
        } else if (NumberUtil.equals(latestSceneVersionDto.getStatus(), SceneConst.SCENE_VERSION_STATUS__CONFIGURING)) {
            // update old version name
            latestSceneVersionDto.setVersion(LocalDateTimeUtil.format(LocalDateTime.now(), DatePattern.PURE_DATETIME_PATTERN));
            sceneVersionDao.updateById(SceneVersionPoConverter.createFrom(latestSceneVersionDto));
        } else {
            // build new version
            latestSceneVersionDto = new SceneVersionDto();
            latestSceneVersionDto.setSceneId(sceneId);
            latestSceneVersionDto.setVersion(LocalDateTimeUtil.format(LocalDateTime.now(), DatePattern.PURE_DATETIME_PATTERN));
            latestSceneVersionDto.setStatus(SceneConst.SCENE_VERSION_STATUS__CONFIGURING);
            SceneVersionPo sceneVersionPo = SceneVersionPoConverter.createFrom(latestSceneVersionDto);
            sceneVersionDao.insert(sceneVersionPo);
            latestSceneVersionDto.setId(sceneVersionPo.getId());
        }

        // 查询版本下的流程
        List<VersionProcessDto> versionProcessDtoList =
                versionProcessService.fetchListByVersionId(ListUtil.of(id), false).get(id);

        // delete old version process
        Long latestVersionId = latestSceneVersionDto.getId();
        List<VersionProcessDto> latestVersionProcessDtoList =
                versionProcessService.fetchListByVersionId(ListUtil.of(latestVersionId), true).get(latestVersionId);
        if (CollUtil.isNotEmpty(latestVersionProcessDtoList)) {
            List<Long> idList = latestVersionProcessDtoList.stream().map(VersionProcessDto::getId).collect(Collectors.toList());
            versionProcessService.deleteById(idList);
        }

        // build new version process
        for (VersionProcessDto versionProcessDto : latestVersionProcessDtoList) {
            BpmnXmlDto bpmnXmlDto = new BpmnXmlDto();
            bpmnXmlDto.setProcessId(versionProcessDto.getProcessId());
            bpmnXmlDto.setProcessName(versionProcessDto.getProcessName());
            bpmnXmlDto.setProcessXml(versionProcessDto.getProcessXml());
            BpmnXmlDto newBpmnXmlDto = processDefinitionService.replaceProcessId(bpmnXmlDto);
            VersionProcessDto newVersionProcessDto = VersionProcessDtoConverter
                    .createFrom(latestVersionId, newBpmnXmlDto, versionProcessDto.getProcessImg());
            versionProcessService.save(newVersionProcessDto);
        }

        return latestSceneVersionDto;
    }

    @Override
    public boolean checkNewReleaseVersion(Long sceneId, String version) {
        Assert.notNull(sceneId, "场景id不能为空");
        Assert.notNull(version, "版本号不能为空");
        SceneVersionDto latestSceneVersionDto = fetchLatestOneBySceneId(ListUtil.of(sceneId),
                ListUtil.of(SceneConst.SCENE_VERSION_STATUS__RUNNING, SceneConst.SCENE_VERSION_STATUS__STOPPED)).get(sceneId);
        SceneReleaseVersionBo sceneReleaseVersionBo = parseReleaseVersion(version);
        if(latestSceneVersionDto == null) return true;

        String latestVersion = latestSceneVersionDto.getVersion();
        SceneReleaseVersionBo latestSceneReleaseVersionBo = parseReleaseVersion(latestVersion);

        if(NumberUtil.compare(sceneReleaseVersionBo.getMajor(), latestSceneReleaseVersionBo.getMajor()) < 0) return false;
        if(NumberUtil.compare(sceneReleaseVersionBo.getMajor(), latestSceneReleaseVersionBo.getMajor()) > 0) return true;
        if(NumberUtil.compare(sceneReleaseVersionBo.getMinor(), latestSceneReleaseVersionBo.getMinor()) < 0) return false;
        if(NumberUtil.compare(sceneReleaseVersionBo.getMinor(), latestSceneReleaseVersionBo.getMinor()) > 0) return true;
        if(NumberUtil.compare(sceneReleaseVersionBo.getPatch(), latestSceneReleaseVersionBo.getPatch()) < 0) return false;
        if(NumberUtil.compare(sceneReleaseVersionBo.getPatch(), latestSceneReleaseVersionBo.getPatch()) > 0) return true;
        throw new RuntimeException("版本号不能相同");
    }

    private SceneReleaseVersionBo parseReleaseVersion(String name) {
        Assert.isTrue(ReUtil.isMatch(SceneReleaseVersionBo.PATTERN, name), "版本名称格式不正确");
        List<String> groups = ReUtil.getAllGroups(SceneReleaseVersionBo.PATTERN, name, false);

        SceneReleaseVersionBo bo = new SceneReleaseVersionBo();
        bo.setMajor(Integer.parseInt(groups.get(0)));
        bo.setMinor(Integer.parseInt(groups.get(1)));
        bo.setPatch(Integer.parseInt(groups.get(2)));
        bo.setDate(LocalDateTimeUtil.parseDate(groups.get(3), DatePattern.PURE_DATE_PATTERN));
        return bo;
    }

    private SceneVersionDto getAndCheckStatus(Long id, int[] statusArr) {
        Assert.notNull(id, "版本ID不能为空");

        SceneVersionDto versionDto = fetchById(ListUtil.of(id)).get(id);
        Assert.notNull(versionDto, "版本ID不存在。ID: {}", id);

        boolean flag = false;
        for (int status : statusArr) {
            if (NumberUtil.equals(versionDto.getStatus(), status)) {
                flag = true;
                break;
            }
        }

        if (!flag) {
            throw new RuntimeException(StrUtil.format("版本状态异常，id: {}", id));
        } else {
            return versionDto;
        }

    }

}
