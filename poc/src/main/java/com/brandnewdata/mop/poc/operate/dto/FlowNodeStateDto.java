package com.brandnewdata.mop.poc.operate.dto;

import com.brandnewdata.mop.poc.operate.entity.FlowNodeState;

public enum FlowNodeStateDto {
    ACTIVE,
    INCIDENT,
    COMPLETED,
    TERMINATED,
    UNSPECIFIED,
    UNKNOWN;


    public static FlowNodeStateDto getState(FlowNodeState state) {
        if (state == null) {
            return UNSPECIFIED;
        }
        FlowNodeStateDto stateDto = FlowNodeStateDto.valueOf(state.name());
        if (stateDto != null) return stateDto;
        return UNKNOWN;
    }
}
