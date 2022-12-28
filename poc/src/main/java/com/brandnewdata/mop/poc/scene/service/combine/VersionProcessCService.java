package com.brandnewdata.mop.poc.scene.service.combine;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.brandnewdata.mop.poc.constant.SceneConst;
import com.brandnewdata.mop.poc.env.dto.EnvDto;
import com.brandnewdata.mop.poc.env.service.IEnvService;
import com.brandnewdata.mop.poc.process.dto.BpmnXmlDto;
import com.brandnewdata.mop.poc.process.service.IProcessDefinitionService;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService;
import com.brandnewdata.mop.poc.scene.converter.VersionProcessPoConverter;
import com.brandnewdata.mop.poc.scene.dao.VersionProcessDao;
import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;
import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;
import com.brandnewdata.mop.poc.scene.po.VersionProcessPo;
import com.brandnewdata.mop.poc.scene.service.atomic.ISceneVersionAService;
import com.brandnewdata.mop.poc.scene.service.atomic.IVersionProcessAService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class VersionProcessCService implements IVersionProcessCService{

    @Resource
    private VersionProcessDao versionProcessDao;

    private final IEnvService envService;

    private final IProcessDeployService processDeployService;

    private final IVersionProcessAService versionProcessAService;

    private final ISceneVersionAService sceneVersionAService;

    private final IProcessDefinitionService processDefinitionService;

    public VersionProcessCService(IEnvService envService,
                                  IProcessDeployService processDeployService,
                                  IVersionProcessAService versionProcessAService,
                                  ISceneVersionAService sceneVersionAService,
                                  IProcessDefinitionService processDefinitionService) {
        this.envService = envService;
        this.processDeployService = processDeployService;
        this.versionProcessAService = versionProcessAService;
        this.sceneVersionAService = sceneVersionAService;
        this.processDefinitionService = processDefinitionService;
    }

    @Override
    public VersionProcessDto save(VersionProcessDto versionProcessDto) {
        Long versionId = versionProcessDto.getVersionId();
        sceneVersionAService.fetchOneByIdAndCheckStatus(versionId, new int[]{SceneConst.SCENE_VERSION_STATUS__CONFIGURING});

        BpmnXmlDto bpmnXmlDto = new BpmnXmlDto(versionProcessDto.getProcessId(),
                versionProcessDto.getProcessName(), versionProcessDto.getProcessXml());
        bpmnXmlDto = processDefinitionService.baseCheck(bpmnXmlDto);

        String processId = bpmnXmlDto.getProcessId();
        String processName = bpmnXmlDto.getProcessName();
        String processXml = bpmnXmlDto.getProcessXml();
        String processImg = versionProcessDto.getProcessImg();

        Long id = versionProcessDto.getId();

        if(id == null) {
            // 手动指定
            versionProcessDto.setId(IdUtil.getSnowflakeNextId());

            VersionProcessPo versionProcessPo = VersionProcessPoConverter.createFrom(versionProcessDto);
            VersionProcessPoConverter.updateFrom(versionProcessPo,processId, processName, processXml, processImg);
            versionProcessPo.setProcessXml(processXml);
            versionProcessDao.insert(versionProcessPo);
        } else {
            versionProcessDto = versionProcessAService.fetchOneById(ListUtil.of(id)).get(id);
            if(!StrUtil.equals(versionProcessDto.getProcessId(), processId)) {
                throw new RuntimeException("流程id不能改变");
            }

            VersionProcessPo versionProcessPo = VersionProcessPoConverter.createFrom(versionProcessDto);
            VersionProcessPoConverter.updateFrom(versionProcessPo,processId, processName, processXml, processImg);
            versionProcessDao.updateById(versionProcessPo);
        }

        return versionProcessDto;
    }

    @Override
    public void debug(VersionProcessDto dto, Map<String, Object> variables) {
        Long versionId = dto.getVersionId();
        Assert.notNull(versionId, "流程版本id不能为空");
        SceneVersionDto sceneVersionDto = sceneVersionAService.fetchById(ListUtil.of(versionId)).get(versionId);
        Assert.notNull(sceneVersionDto, "版本不存在。version id：{}", versionId);
        Assert.isTrue(NumberUtil.equals(sceneVersionDto.getStatus(), SceneConst.SCENE_VERSION_STATUS__DEBUGGING)
                        || NumberUtil.equals(sceneVersionDto.getStatus(), SceneConst.SCENE_VERSION_STATUS__CONFIGURING),
                "版本状态异常。仅支持以下状态：调试中");

        // 查询流程
        Long id = dto.getId();
        Assert.notNull(id, "流程不能为空");
        VersionProcessDto versionProcessDto = versionProcessAService.fetchOneById(ListUtil.of(id)).get(id);
        Assert.notNull(versionProcessDto, "流程不存在: {}", id);

        // 查询调试环境
        EnvDto envDto = envService.fetchDebugEnv();
        Assert.notNull(envDto, "调试环境不存在");
        Long envId = envDto.getId();

        processDeployService.startAsync(versionProcessDto.getProcessId(), Opt.ofNullable(variables).orElse(MapUtil.empty()), envId);
    }

    @Override
    public void deleteById(Long id) {
        Assert.notNull(id, "id must not null");
        VersionProcessDto versionProcessDto = versionProcessAService.fetchOneById(ListUtil.of(id)).get(id);
        Assert.notNull(versionProcessDto, "id must not exist");
        sceneVersionAService.fetchOneByIdAndCheckStatus(versionProcessDto.getVersionId(),
                new int[]{SceneConst.SCENE_VERSION_STATUS__CONFIGURING, SceneConst.SCENE_VERSION_STATUS__STOPPED});

        UpdateWrapper<VersionProcessPo> update = new UpdateWrapper<>();
        update.setSql(StrUtil.format("{}={}", VersionProcessPo.DELETE_FLAG, VersionProcessPo.ID));
        update.eq(VersionProcessPo.ID, id);

        versionProcessDao.update(null, update);
    }

    @Override
    public void deleteByVersionId(Long versionId) {
        sceneVersionAService.fetchOneByIdAndCheckStatus(versionId,
                new int[]{SceneConst.SCENE_VERSION_STATUS__CONFIGURING, SceneConst.SCENE_VERSION_STATUS__STOPPED});

        UpdateWrapper<VersionProcessPo> update = new UpdateWrapper<>();
        update.setSql(StrUtil.format("{}={}", VersionProcessPo.DELETE_FLAG, VersionProcessPo.ID));
        update.eq(VersionProcessPo.VERSION_ID, versionId);

        versionProcessDao.update(null, update);
    }


}
