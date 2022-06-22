/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.OperationEntity
 *  io.camunda.operate.entities.OperationState
 *  io.camunda.operate.entities.OperationType
 *  io.camunda.operate.webapp.rest.dto.CreatableFromEntity
 */
package io.camunda.operate.webapp.rest.dto;

import io.camunda.operate.entities.OperationEntity;
import io.camunda.operate.entities.OperationState;
import io.camunda.operate.entities.OperationType;
import io.camunda.operate.webapp.rest.dto.CreatableFromEntity;
import java.util.Objects;

public class OperationDto
implements CreatableFromEntity<OperationDto, OperationEntity> {
    private String id;
    private String batchOperationId;
    private OperationType type;
    private OperationState state;
    private String errorMessage;

    public String getId() {
        return this.id;
    }

    public OperationDto setId(String id) {
        this.id = id;
        return this;
    }

    public String getBatchOperationId() {
        return this.batchOperationId;
    }

    public OperationDto setBatchOperationId(String batchOperationId) {
        this.batchOperationId = batchOperationId;
        return this;
    }

    public OperationType getType() {
        return this.type;
    }

    public OperationDto setType(OperationType type) {
        this.type = type;
        return this;
    }

    public OperationState getState() {
        return this.state;
    }

    public OperationDto setState(OperationState state) {
        this.state = state;
        return this;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public OperationDto setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public OperationDto fillFrom(OperationEntity operationEntity) {
        this.setId(operationEntity.getId()).setType(operationEntity.getType()).setState(operationEntity.getState()).setErrorMessage(operationEntity.getErrorMessage()).setBatchOperationId(operationEntity.getBatchOperationId());
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        OperationDto that = (OperationDto)o;
        return Objects.equals(this.id, that.id) && Objects.equals(this.batchOperationId, that.batchOperationId) && this.type == that.type && this.state == that.state && Objects.equals(this.errorMessage, that.errorMessage);
    }

    public int hashCode() {
        return Objects.hash(this.id, this.batchOperationId, this.type, this.state, this.errorMessage);
    }
}
