package com.brandnewdata.mop.poc.bff.converter.scene;

import com.brandnewdata.mop.poc.bff.vo.scene.operate.OperateProcessInstanceVo;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDto;
import com.brandnewdata.mop.poc.process.dto.ProcessReleaseDeployDto;
import com.brandnewdata.mop.poc.process.po.ProcessReleaseDeployPo;
import com.brandnewdata.mop.poc.scene.dto.SceneReleaseDeployDto;

public class OperateProcessInstanceVoConverter {

    public static OperateProcessInstanceVo createFrom(ListViewProcessInstanceDto listViewProcessInstanceDto) {
        OperateProcessInstanceVo vo = new OperateProcessInstanceVo();
        vo.setInstanceId(listViewProcessInstanceDto.getId());
        vo.setState(listViewProcessInstanceDto.getState().name());
        vo.setStartTime(listViewProcessInstanceDto.getStartDate());
        vo.setEndTime(listViewProcessInstanceDto.getEndDate());
        return vo;
    }

    public static void updateFrom(OperateProcessInstanceVo vo, SceneReleaseDeployDto sceneReleaseDeployDto) {
        vo.setEnvId(sceneReleaseDeployDto.getEnvId());
        vo.setSceneId(sceneReleaseDeployDto.getSceneId());
        vo.setSceneName(sceneReleaseDeployDto.getSceneName());
        vo.setVersionId(sceneReleaseDeployDto.getVersionId());
        vo.setVersionName(sceneReleaseDeployDto.getVersionName());
        vo.setProcessId(sceneReleaseDeployDto.getProcessId());
        vo.setProcessName(sceneReleaseDeployDto.getProcessName());
    }

    public static void updateFrom(OperateProcessInstanceVo vo, ProcessReleaseDeployDto processReleaseDeployDto) {
        vo.setReleaseDeployId(processReleaseDeployDto.getId());
    }
}
