package com.brandnewdata.mop.poc.operate.converter;

import com.brandnewdata.mop.poc.operate.dto.SequenceFlowDto;
import com.brandnewdata.mop.poc.operate.po.SequenceFlowPo;

public class SequenceFlowDtoConverter {

    public static SequenceFlowDto createFrom(SequenceFlowPo entity) {
        SequenceFlowDto dto = new SequenceFlowDto();
        dto.setId(entity.getId());
        dto.setActivityId(entity.getActivityId());
        dto.setProcessInstanceKey(dto.getProcessInstanceKey());
        return dto;
    }
}
