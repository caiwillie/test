package com.brandnewdata.mop.poc.scene.service.combine;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.brandnewdata.mop.poc.constant.SceneConst;
import com.brandnewdata.mop.poc.process.dto.BpmnXmlDto;
import com.brandnewdata.mop.poc.process.manager.ConnectorManager;
import com.brandnewdata.mop.poc.process.service.IProcessDefinitionService;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService;
import com.brandnewdata.mop.poc.process.util.ProcessUtil;
import com.brandnewdata.mop.poc.scene.converter.SceneReleaseDeployDtoConverter;
import com.brandnewdata.mop.poc.scene.converter.SceneVersionPoConverter;
import com.brandnewdata.mop.poc.scene.converter.VersionProcessDtoConverter;
import com.brandnewdata.mop.poc.scene.dao.SceneVersionDao;
import com.brandnewdata.mop.poc.scene.dto.SceneReleaseDeployDto;
import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;
import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;
import com.brandnewdata.mop.poc.scene.po.SceneVersionPo;
import com.brandnewdata.mop.poc.scene.service.atomic.ISceneReleaseDeployAService;
import com.brandnewdata.mop.poc.scene.service.atomic.IVersionProcessAService;
import com.brandnewdata.mop.poc.scene.service.atomic.SceneVersionAService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static com.brandnewdata.mop.poc.constant.ProcessConst.PROCESS_BIZ_TYPE__SCENE;

@Slf4j
@Service
public class SceneVersionCService implements ISceneVersionCService {

    @Resource
    private SceneVersionDao sceneVersionDao;

    private final SceneVersionAService sceneVersionAService;

    private final IVersionProcessAService versionProcessAService;

    private final IVersionProcessCService versionProcessCService;

    private final ISceneReleaseDeployAService sceneReleaseDeployService;

    private final IProcessDeployService processDeployService;

    private final IProcessDefinitionService processDefinitionService;

    private final ConnectorManager connectorManager;

    public SceneVersionCService(SceneVersionAService sceneVersionAService,
                                IVersionProcessAService versionProcessAService,
                                IVersionProcessCService versionProcessCService,
                                IProcessDeployService processDeployService,
                                ISceneReleaseDeployAService sceneReleaseDeployService,
                                IProcessDefinitionService processDefinitionService,
                                ConnectorManager connectorManager) {
        this.sceneVersionAService = sceneVersionAService;
        this.versionProcessAService = versionProcessAService;
        this.versionProcessCService = versionProcessCService;
        this.processDeployService = processDeployService;
        this.sceneReleaseDeployService = sceneReleaseDeployService;
        this.processDefinitionService = processDefinitionService;
        this.connectorManager = connectorManager;
    }

    @Override
    public SceneVersionDto debug(Long id, Long envId) {
        Assert.notNull(id, "版本id不能为空");
        SceneVersionDto sceneVersionDto = sceneVersionAService.fetchById(ListUtil.of(id)).get(id);
        Assert.notNull(sceneVersionDto, "版本不存在。version id：{}", id);

        // 获取版本下的流程
        List<VersionProcessDto> versionProcessDtoList =
                versionProcessAService.fetchListByVersionId(ListUtil.of(id), false).get(id);
        Assert.isTrue(CollUtil.isNotEmpty(versionProcessDtoList), "该版本下至少需要配置一个流程");

        Assert.notNull(envId, "环境id不能为空");

        for (VersionProcessDto versionProcessDto : versionProcessDtoList) {
            BpmnXmlDto deployDto = new BpmnXmlDto();
            deployDto.setProcessId(versionProcessDto.getProcessId());
            deployDto.setProcessName(versionProcessDto.getProcessName());
            deployDto.setProcessXml(versionProcessDto.getProcessXml());
            processDeployService.snapshotDeploy(deployDto, envId, PROCESS_BIZ_TYPE__SCENE);
        }

        // 修改状态
        sceneVersionDto.setDeployStatus(SceneConst.SCENE_DEPLOY_STATUS_SNAPSHOT_UNDEPLOY);
        sceneVersionDto.setDeployProgressPercentage(0.0);
        sceneVersionDto.setExceptionMessage(null);
        sceneVersionDao.updateById(SceneVersionPoConverter.createFrom(sceneVersionDto));
        return sceneVersionDto;
    }

    @Override
    public SceneVersionDto stopDebug(Long id, Long envId) {
        SceneVersionDto sceneVersionDto = sceneVersionAService.fetchOneByIdAndCheckStatus(id, new int[]{SceneConst.SCENE_VERSION_STATUS__DEBUGGING});

        // 修改状态
        sceneVersionDto.setStatus(SceneConst.SCENE_VERSION_STATUS__CONFIGURING);
        sceneVersionDao.updateById(SceneVersionPoConverter.createFrom(sceneVersionDto));
        return sceneVersionDto;
    }

    @Override
    public SceneVersionDto stop(Long id) {
        SceneVersionDto sceneVersionDto = sceneVersionAService.fetchOneByIdAndCheckStatus(id, new int[]{SceneConst.SCENE_VERSION_STATUS__RUNNING});

        // stop process
        List<SceneReleaseDeployDto> sceneReleaseDeployDtoList =
                sceneReleaseDeployService.fetchListByVersionId(ListUtil.of(id)).get(id);
        for (SceneReleaseDeployDto sceneReleaseDeployDto : sceneReleaseDeployDtoList) {
            connectorManager.stopVersionProcess(sceneReleaseDeployDto);
        }

        // 修改状态
        sceneVersionDto.setStatus(SceneConst.SCENE_VERSION_STATUS__STOPPED);
        sceneVersionDao.updateById(SceneVersionPoConverter.createFrom(sceneVersionDto));
        return sceneVersionDto;
    }

    @Override
    public SceneVersionDto resume(Long id, List<Long> envIdList) {
        SceneVersionDto sceneVersionDto = sceneVersionAService.fetchOneByIdAndCheckStatus(id, new int[]{SceneConst.SCENE_VERSION_STATUS__STOPPED});
        Assert.notEmpty(envIdList, "环境列表不能为空");

        // resume process
        List<SceneReleaseDeployDto> sceneReleaseDeployDtoList =
                sceneReleaseDeployService.fetchListByVersionId(ListUtil.of(id)).get(id);
        for (SceneReleaseDeployDto sceneReleaseDeployDto : sceneReleaseDeployDtoList) {
            connectorManager.resumeVersionProcess(sceneReleaseDeployDto);
        }

        // 修改状态
        sceneVersionDto.setStatus(SceneConst.SCENE_VERSION_STATUS__RUNNING);
        sceneVersionDao.updateById(SceneVersionPoConverter.createFrom(sceneVersionDto));
        return sceneVersionDto;
    }

    @Override
    @Transactional
    public SceneVersionDto deploy(Long id, String sceneName, List<Long> envIdList, String version) {
        // 配置中，和调试中均可以进行发布
        SceneVersionDto sceneVersionDto = sceneVersionAService.fetchOneByIdAndCheckStatus(id,
                new int[]{SceneConst.SCENE_VERSION_STATUS__CONFIGURING, SceneConst.SCENE_VERSION_STATUS__DEBUGGING});
        Assert.notEmpty(envIdList, "环境列表不能为空");
        Assert.notNull(version, "版本不能为空");
        Assert.isTrue(sceneVersionAService.checkNewReleaseVersion(sceneVersionDto.getSceneId(), version), "新版本号必须大于旧版本号");
        sceneVersionDto.setVersion(version);

        // 查询版本下的流程
        List<VersionProcessDto> versionProcessDtoList =
                versionProcessAService.fetchListByVersionId(ListUtil.of(id), false).get(id);
        Assert.isTrue(CollUtil.isNotEmpty(versionProcessDtoList), "该版本下至少需要配置一个流程");

        // 发布流程
        for (VersionProcessDto versionProcessDto : versionProcessDtoList) {
            for (Long envId : envIdList) {
                // release deploy
                BpmnXmlDto deployDto = new BpmnXmlDto();
                deployDto.setProcessId(versionProcessDto.getProcessId());
                deployDto.setProcessName(versionProcessDto.getProcessName());
                deployDto.setProcessXml(versionProcessDto.getProcessXml());
                processDeployService.releaseDeploy(deployDto, envId, PROCESS_BIZ_TYPE__SCENE);

                // 保存关系
                SceneReleaseDeployDto sceneReleaseDeployDto = new SceneReleaseDeployDto();
                SceneReleaseDeployDtoConverter.updateFrom(sceneReleaseDeployDto, versionProcessDto);
                SceneReleaseDeployDtoConverter.updateFrom(sceneReleaseDeployDto, sceneVersionDto);
                sceneReleaseDeployDto.setSceneName(sceneName);
                sceneReleaseDeployDto.setEnvId(envId);
                sceneReleaseDeployService.save(sceneReleaseDeployDto);
            }
        }

        // 删除之前在其他环境发布过的旧记录
        sceneReleaseDeployService.deleteByVersionIdAndExceptEnvId(id, envIdList);

        // 更新状态
        sceneVersionDto.setDeployStatus(SceneConst.SCENE_DEPLOY_STATUS_RELEASE_UNDEPLOY);
        sceneVersionDto.setDeployProgressPercentage(0.0);
        sceneVersionDto.setExceptionMessage(null);
        sceneVersionDao.updateById(SceneVersionPoConverter.createFrom(sceneVersionDto));
        return sceneVersionDto;
    }

    @Override
    @Transactional
    public SceneVersionDto copyToNew(Long id) {
        SceneVersionDto oldSceneVersionDto = sceneVersionAService.fetchOneByIdAndCheckStatus(id, new int[]{SceneConst.SCENE_VERSION_STATUS__RUNNING,
                SceneConst.SCENE_VERSION_STATUS__STOPPED});

        Long sceneId = oldSceneVersionDto.getSceneId();

        // check latest version status
        SceneVersionDto latestSceneVersionDto = sceneVersionAService.fetchLatestOneBySceneId(ListUtil.of(sceneId), null).get(sceneId);
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
                versionProcessAService.fetchListByVersionId(ListUtil.of(id), false).get(id);

        // delete old version process
        Long latestVersionId = latestSceneVersionDto.getId();
        versionProcessCService.deleteByVersionId(latestVersionId);

        // build new version process
        for (VersionProcessDto versionProcessDto : versionProcessDtoList) {
            BpmnXmlDto bpmnXmlDto = new BpmnXmlDto();
            bpmnXmlDto.setProcessId(ProcessUtil.generateProcessId());
            bpmnXmlDto.setProcessName(versionProcessDto.getProcessName());
            bpmnXmlDto.setProcessXml(versionProcessDto.getProcessXml());
            BpmnXmlDto newBpmnXmlDto = processDefinitionService.baseCheck(bpmnXmlDto);
            VersionProcessDto newVersionProcessDto = VersionProcessDtoConverter
                    .createFrom(latestVersionId, newBpmnXmlDto, versionProcessDto.getProcessImg());
            versionProcessCService.save(newVersionProcessDto);
        }

        return latestSceneVersionDto;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SceneVersionDto sceneVersionDto = sceneVersionAService.fetchOneByIdAndCheckStatus(id,
                new int[]{SceneConst.SCENE_VERSION_STATUS__CONFIGURING, SceneConst.SCENE_VERSION_STATUS__STOPPED});
        Long sceneId = sceneVersionDto.getSceneId();
        Long count = sceneVersionAService.countBySceneId(ListUtil.of(sceneId)).get(sceneId);
        Assert.isTrue(count > 1, "删除失败，最后一个版本无法删除");

        // 删除版本下的流程
        versionProcessCService.deleteByVersionId(id);

        // 删除已发布的记录
        sceneReleaseDeployService.deleteByVersionId(id);

        // 删除版本
        UpdateWrapper<SceneVersionPo> update = new UpdateWrapper<>();
        update.setSql(StrUtil.format("{}={}", SceneVersionPo.DELETE_FLAG, SceneVersionPo.ID));
        update.eq(SceneVersionPo.ID, id);
        sceneVersionDao.update(null, update);
    }

    @Override
    @Transactional
    public void deleteBySceneId(Long sceneId) {
        List<SceneVersionDto> sceneVersionDtoList = sceneVersionAService.fetchListBySceneId(ListUtil.of(sceneId)).get(sceneId);
        for (SceneVersionDto sceneVersionDto : sceneVersionDtoList) {
            Integer status = sceneVersionDto.getStatus();
            if(NumberUtil.equals(status, SceneConst.SCENE_VERSION_STATUS__RUNNING)
                    || NumberUtil.equals(status, SceneConst.SCENE_VERSION_STATUS__DEBUGGING)) {
                throw new RuntimeException(StrUtil.format("调试中和运行中的版本不能删除. 版本: {}", sceneVersionDto.getVersion()));
            }
        }

        for (SceneVersionDto sceneVersionDto : sceneVersionDtoList) {
            // 删除版本下的流程
            versionProcessCService.deleteByVersionId(sceneVersionDto.getId());
        }

        // 删除版本
        UpdateWrapper<SceneVersionPo> update = new UpdateWrapper<>();
        update.setSql(StrUtil.format("{}={}", SceneVersionPo.DELETE_FLAG, SceneVersionPo.ID));
        update.eq(SceneVersionPo.SCENE_ID, sceneId);
        sceneVersionDao.update(null, update);
        sceneVersionDao.update(null, update);
    }


}
