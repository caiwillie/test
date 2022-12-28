package com.brandnewdata.mop.poc.scene.service.combine;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.constant.SceneConst;
import com.brandnewdata.mop.poc.process.dto.BpmnXmlDto;
import com.brandnewdata.mop.poc.process.service.IProcessDefinitionService;
import com.brandnewdata.mop.poc.scene.converter.VersionProcessPoConverter;
import com.brandnewdata.mop.poc.scene.dao.VersionProcessDao;
import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;
import com.brandnewdata.mop.poc.scene.po.VersionProcessPo;
import com.brandnewdata.mop.poc.scene.service.atomic.ISceneVersionAService;
import com.brandnewdata.mop.poc.scene.service.atomic.IVersionProcessAService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class VersionProcessCService implements IVersionProcessCService{

    @Resource
    private VersionProcessDao versionProcessDao;

    private final IVersionProcessAService versionProcessAService;

    private final ISceneVersionAService sceneVersionAService;

    private final IProcessDefinitionService processDefinitionService;

    public VersionProcessCService(IVersionProcessAService versionProcessAService,
                                  ISceneVersionAService sceneVersionAService,
                                  IProcessDefinitionService processDefinitionService) {
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
            VersionProcessDto updateContent = versionProcessDto;
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
    public void deleteById(List<Long> idList) {
        if(CollUtil.isEmpty(idList)) return;
        Assert.isFalse(CollUtil.hasNull(idList), "版本id列表不能含有空值");
        for (Long id : idList) {
            versionProcessDao.deleteById(id);
        }
    }


}
