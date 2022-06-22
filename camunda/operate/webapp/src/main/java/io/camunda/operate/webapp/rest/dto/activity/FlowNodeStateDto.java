/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.FlowNodeState
 */
package io.camunda.operate.webapp.rest.dto.activity;

import io.camunda.operate.entities.FlowNodeState;

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
