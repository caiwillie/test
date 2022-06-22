/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.listview.ProcessInstanceState
 */
package io.camunda.operate.webapp.rest.dto.listview;

import io.camunda.operate.entities.listview.ProcessInstanceState;

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
