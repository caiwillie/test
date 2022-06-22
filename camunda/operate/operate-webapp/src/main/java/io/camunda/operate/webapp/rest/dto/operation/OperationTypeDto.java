/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.OperationType
 */
package io.camunda.operate.webapp.rest.dto.operation;

import io.camunda.operate.entities.OperationType;

public enum OperationTypeDto {
    RESOLVE_INCIDENT,
    CANCEL_PROCESS_INSTANCE,
    DELETE_PROCESS_INSTANCE,
    ADD_VARIABLE,
    UPDATE_VARIABLE,
    UNSPECIFIED,
    UNKNOWN;


    public static OperationTypeDto getType(OperationType type) {
        if (type == null) {
            return UNSPECIFIED;
        }
        OperationTypeDto typeDto = OperationTypeDto.valueOf(type.name());
        if (typeDto != null) return typeDto;
        return UNKNOWN;
    }
}
