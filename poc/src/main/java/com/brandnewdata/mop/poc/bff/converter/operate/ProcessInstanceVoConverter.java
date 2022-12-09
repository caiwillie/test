package com.brandnewdata.mop.poc.bff.converter.operate;

import cn.hutool.core.lang.Opt;
import com.brandnewdata.mop.poc.bff.vo.operate.process.ProcessInstanceVo;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDto;
import com.brandnewdata.mop.poc.operate.dto.ProcessInstanceStateDto;

public class ProcessInstanceVoConverter {

    public static ProcessInstanceVo createFrom(ListViewProcessInstanceDto dto) {
        ProcessInstanceVo vo = new ProcessInstanceVo();
        vo.setInstanceId(dto.getId());
        vo.setProcessId(dto.getBpmnProcessId());
        vo.setParentInstanceId(dto.getParentInstanceId());
        vo.setStartTime(dto.getStartDate());
        vo.setEndTime(dto.getEndDate());
        vo.setState(Opt.ofNullable(dto.getState()).map(ProcessInstanceStateDto::name).orElse(null));
        return vo;
    }
}
