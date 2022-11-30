package com.brandnewdata.mop.poc.operate.converter;

import com.brandnewdata.mop.poc.operate.dao.SequenceFlowDao;
import com.brandnewdata.mop.poc.operate.dto.SequenceFlowDto;
import com.brandnewdata.mop.poc.operate.entity.SequenceFlowEntity;

public class SequenceFlowDtoConverter {

    public static SequenceFlowDto createFrom(SequenceFlowEntity entity) {
        SequenceFlowDto dto = new SequenceFlowDto();
        dto.setId(entity.getId());
        dto.setActivityId(entity.getActivityId());
        dto.setProcessInstanceKey(dto.getProcessInstanceKey());
        return dto;
    }
}
