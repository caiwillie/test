package com.brandnewdata.mop.poc.bff.service.scene;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.bff.converter.process.ProcessDefinitionVoConverter;
import com.brandnewdata.mop.poc.bff.converter.scene.OperateProcessInstanceVoConverter;
import com.brandnewdata.mop.poc.bff.vo.process.ProcessDefinitionVo;
import com.brandnewdata.mop.poc.bff.vo.scene.operate.OperateProcessInstanceVo;
import com.brandnewdata.mop.poc.bff.vo.scene.operate.SceneDeployFilter;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDto;
import com.brandnewdata.mop.poc.operate.service.IProcessInstanceService2;
import com.brandnewdata.mop.poc.process.dto.ProcessReleaseDeployDto;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService2;
import com.brandnewdata.mop.poc.scene.dto.SceneReleaseDeployDto;
import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;
import com.brandnewdata.mop.poc.scene.service.ISceneReleaseDeployService;
import com.brandnewdata.mop.poc.scene.service.IVersionProcessService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SceneOperateBffService2 {

    private final ISceneReleaseDeployService sceneReleaseDeployService;

    private final IProcessDeployService2 processDeployService;

    private final IProcessInstanceService2 processInstanceService;

    private final IVersionProcessService versionProcessService;

    public SceneOperateBffService2(ISceneReleaseDeployService sceneReleaseDeployService,
                                   IProcessDeployService2 processDeployService,
                                   IProcessInstanceService2 processInstanceService,
                                   IVersionProcessService versionProcessService) {
        this.sceneReleaseDeployService = sceneReleaseDeployService;
        this.processDeployService = processDeployService;
        this.processInstanceService = processInstanceService;
        this.versionProcessService = versionProcessService;
    }

    public Page<OperateProcessInstanceVo> pageProcessInstance(SceneDeployFilter filter) {
        Long envId = Assert.notNull(filter.getEnvId());

        Long sceneId = filter.getSceneId();
        Long versionId = filter.getVersionId();
        String processId = filter.getProcessId();

        // 获取符合过滤条件的流程id列表
        List<SceneReleaseDeployDto> sceneReleaseDeployDtoList = sceneReleaseDeployService.fetchByEnvId(envId);
        Map<String, SceneReleaseDeployDto> filterSceneReleaseDeployDtoMap = sceneReleaseDeployDtoList.stream().filter(dto -> {
            if (sceneId == null) return true;
            if (!NumberUtil.equals(sceneId, dto.getSceneId())) return false;

            if (versionId == null) return true;
            if (!NumberUtil.equals(versionId, dto.getVersionId())) return false;

            if (processId == null) return true;
            if (!StrUtil.equals(processId, dto.getProcessId())) {
                return false;
            } else {
                return true;
            }
        }).collect(Collectors.toMap(SceneReleaseDeployDto::getProcessId, Function.identity()));


        // 获取release deploy列表
        Map<String, ProcessReleaseDeployDto> processReleaseDeployDtoMap =
                processDeployService.fetchReleaseByEnvIdAndProcessId(envId, ListUtil.toList(filterSceneReleaseDeployDtoMap.keySet()));

        List<Long> zeebeKeyList = processReleaseDeployDtoMap.values().stream()
                .map(ProcessReleaseDeployDto::getProcessZeebeKey).collect(Collectors.toList());

        // 查询流程实例
        Page<ListViewProcessInstanceDto> page = processInstanceService.pageProcessInstanceByZeebeKey(envId, zeebeKeyList,
                filter.getPageNum(), filter.getPageSize(), new HashMap<>());

        List<OperateProcessInstanceVo> vos = new ArrayList<>();
        for (ListViewProcessInstanceDto listViewProcessInstanceDto : page.getRecords()) {
            String _processId = listViewProcessInstanceDto.getBpmnProcessId();
            OperateProcessInstanceVo vo = OperateProcessInstanceVoConverter.createFrom(listViewProcessInstanceDto);

            SceneReleaseDeployDto sceneReleaseDeployDto = filterSceneReleaseDeployDtoMap.get(_processId);
            OperateProcessInstanceVoConverter.updateFrom(vo, sceneReleaseDeployDto);

            ProcessReleaseDeployDto processReleaseDeployDto = processReleaseDeployDtoMap.get(_processId);
            OperateProcessInstanceVoConverter.updateFrom(vo, processReleaseDeployDto);
            vos.add(vo);
        }

        return new Page<>(page.getTotal(), vos);
    }

    public ProcessDefinitionVo definitionProcessInstance(OperateProcessInstanceVo vo) {
        String processId = vo.getProcessId();
        VersionProcessDto versionProcessDto =
                versionProcessService.fetchVersionProcessByProcessId(ListUtil.of(vo.getProcessId())).get(processId);
        Assert.notNull(versionProcessDto, "流程id不存在");

        return ProcessDefinitionVoConverter.createFrom(vo.getProcessId(), vo.getProcessName(), versionProcessDto.getProcessXml());
    }

}