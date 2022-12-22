package com.brandnewdata.mop.poc.connector.converter;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Opt;
import com.brandnewdata.mop.api.connector.dto.ProcessInstanceDto;
import com.brandnewdata.mop.poc.bff.vo.scene.DebugProcessInstanceVo;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDto;
import com.brandnewdata.mop.poc.process.dto.ProcessSnapshotDeployDto;


public class ProcessInstanceDtoConverter {

    public static ProcessInstanceDto createFrom(ListViewProcessInstanceDto listViewProcessInstanceDto) {
        ProcessInstanceDto dto = new ProcessInstanceDto();
        dto.setInstanceId(listViewProcessInstanceDto.getId());
        dto.setState(Opt.ofNullable(listViewProcessInstanceDto.getState()).map(Enum::name).orElse(null));
        dto.setStartTime(LocalDateTimeUtil.formatNormal(listViewProcessInstanceDto.getStartDate()));
        dto.setEndTime(LocalDateTimeUtil.formatNormal(listViewProcessInstanceDto.getEndDate()));
        return dto;
    }

    public static void updateFrom(ProcessInstanceDto dto, ProcessSnapshotDeployDto snapshotDeployDto) {
        dto.setSnapshotDeployId(snapshotDeployDto.getId());
        dto.setEnvId(snapshotDeployDto.getEnvId());
    }
}
