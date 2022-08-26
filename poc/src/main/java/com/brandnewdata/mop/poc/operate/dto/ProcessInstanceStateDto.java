package com.brandnewdata.mop.poc.operate.dto;

import io.camunda.operate.dto.ProcessInstanceState;

public enum ProcessInstanceStateDto {
    ACTIVE,
    INCIDENT,
    COMPLETED,
    CANCELED,
    UNKNOWN,
    UNSPECIFIED;


    public static ProcessInstanceStateDto getState(ProcessInstanceState state) {
        if (state == null) {
            return UNSPECIFIED;
        }
        ProcessInstanceStateDto stateDto = ProcessInstanceStateDto.valueOf(state.name());
        if (stateDto != null) return stateDto;
        return UNKNOWN;
    }
}
