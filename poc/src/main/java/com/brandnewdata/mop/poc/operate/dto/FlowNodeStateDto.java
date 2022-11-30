package com.brandnewdata.mop.poc.operate.dto;

import com.brandnewdata.mop.poc.operate.po.FlowNodeState;

public enum FlowNodeStateDto {

    /**
     * 激活
     */
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
