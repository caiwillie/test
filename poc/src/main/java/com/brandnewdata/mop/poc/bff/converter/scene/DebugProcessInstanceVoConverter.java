package com.brandnewdata.mop.poc.bff.converter.scene;

import cn.hutool.core.lang.Opt;
import com.brandnewdata.mop.poc.bff.vo.scene.DebugProcessInstanceVo;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDto;
import com.brandnewdata.mop.poc.process.dto.ProcessSnapshotDeployDto;
import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;

public class DebugProcessInstanceVoConverter {

    public static DebugProcessInstanceVo createFrom(ListViewProcessInstanceDto dto) {
        DebugProcessInstanceVo vo = new DebugProcessInstanceVo();
        vo.setInstanceId(dto.getId());
        vo.setState(Opt.ofNullable(dto.getState()).map(Enum::name).orElse(null));
        vo.setStartTime(dto.getStartDate());
        vo.setEndTime(dto.getEndDate());
        return vo;
    }

    public static void updateFrom(DebugProcessInstanceVo vo, VersionProcessDto dto) {
        vo.setProcessId(dto.getProcessId());
        vo.setProcessName(dto.getProcessName());
    }

    public static void updateFrom(DebugProcessInstanceVo vo, ProcessSnapshotDeployDto dto) {
        vo.setSnapshotDeployId(dto.getId());
    }
}
