/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.OperationType
 */
package io.camunda.operate.webapp.rest.dto.operation;

import io.camunda.operate.entities.OperationType;

public class CreateOperationRequestDto {
    private OperationType operationType;
    private String name;
    private String incidentId;
    private String variableScopeId;
    private String variableName;
    private String variableValue;

    public CreateOperationRequestDto() {
    }

    public CreateOperationRequestDto(OperationType operationType) {
        this.operationType = operationType;
    }

    public OperationType getOperationType() {
        return this.operationType;
    }

    public CreateOperationRequestDto setOperationType(OperationType operationType) {
        this.operationType = operationType;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIncidentId() {
        return this.incidentId;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
    }

    public String getVariableScopeId() {
        return this.variableScopeId;
    }

    public void setVariableScopeId(String variableScopeId) {
        this.variableScopeId = variableScopeId;
    }

    public String getVariableName() {
        return this.variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getVariableValue() {
        return this.variableValue;
    }

    public void setVariableValue(String variableValue) {
        this.variableValue = variableValue;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        CreateOperationRequestDto that = (CreateOperationRequestDto)o;
        if (this.operationType != that.operationType) {
            return false;
        }
        if (this.name != null ? !this.name.equals(that.name) : that.name != null) {
            return false;
        }
        if (this.incidentId != null ? !this.incidentId.equals(that.incidentId) : that.incidentId != null) {
            return false;
        }
        if (this.variableScopeId != null ? !this.variableScopeId.equals(that.variableScopeId) : that.variableScopeId != null) {
            return false;
        }
        if (!(this.variableName != null ? !this.variableName.equals(that.variableName) : that.variableName != null)) return this.variableValue != null ? this.variableValue.equals(that.variableValue) : that.variableValue == null;
        return false;
    }

    public int hashCode() {
        int result = this.operationType != null ? this.operationType.hashCode() : 0;
        result = 31 * result + (this.name != null ? this.name.hashCode() : 0);
        result = 31 * result + (this.incidentId != null ? this.incidentId.hashCode() : 0);
        result = 31 * result + (this.variableScopeId != null ? this.variableScopeId.hashCode() : 0);
        result = 31 * result + (this.variableName != null ? this.variableName.hashCode() : 0);
        result = 31 * result + (this.variableValue != null ? this.variableValue.hashCode() : 0);
        return result;
    }

    public String toString() {
        return "CreateOperationRequestDto{operationType=" + this.operationType + ", name='" + this.name + "', incidentId='" + this.incidentId + "', variableScopeId='" + this.variableScopeId + "', variableName='" + this.variableName + "', variableValue='" + this.variableValue + "'}";
    }
}
