package com.brandnewdata.mop.poc.operate.dto;

import com.brandnewdata.mop.poc.operate.entity.FlowNodeState;

public enum FlowNodeStateDTO {

    /**
     * 激活
     */
    ACTIVE,
    INCIDENT,

    COMPLETED,

    TERMINATED,
    UNSPECIFIED,
    UNKNOWN;


    public static FlowNodeStateDTO getState(FlowNodeState state) {
        if (state == null) {
            return UNSPECIFIED;
        }
        FlowNodeStateDTO stateDto = FlowNodeStateDTO.valueOf(state.name());
        if (stateDto != null) return stateDto;
        return UNKNOWN;
    }
}
