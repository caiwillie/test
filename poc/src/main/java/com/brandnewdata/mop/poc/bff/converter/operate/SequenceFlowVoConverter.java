package com.brandnewdata.mop.poc.bff.converter.operate;

import com.brandnewdata.mop.poc.bff.vo.operate.process.SequenceFlowVo;
import com.brandnewdata.mop.poc.operate.dto.SequenceFlowDto;

public class SequenceFlowVoConverter {

    public static SequenceFlowVo createFrom(SequenceFlowDto dto) {
        SequenceFlowVo vo = new SequenceFlowVo();
        vo.setProcessInstanceId(String.valueOf(dto.getProcessInstanceKey()));
        vo.setSequenceFlowId(dto.getActivityId());
        return vo;
    }
}
