/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.dmn.DecisionInstanceState
 */
package io.camunda.operate.webapp.rest.dto.dmn;

import io.camunda.operate.entities.dmn.DecisionInstanceState;

public enum DecisionInstanceStateDto {
    FAILED,
    EVALUATED,
    UNKNOWN,
    UNSPECIFIED;


    public static DecisionInstanceStateDto getState(DecisionInstanceState state) {
        if (state == null) {
            return UNSPECIFIED;
        }
        DecisionInstanceStateDto stateDto = DecisionInstanceStateDto.valueOf(state.name());
        if (stateDto != null) return stateDto;
        return UNKNOWN;
    }
}
