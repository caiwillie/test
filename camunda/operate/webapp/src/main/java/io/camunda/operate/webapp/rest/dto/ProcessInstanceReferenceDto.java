/*
 * Decompiled with CFR 0.152.
 */
package io.camunda.operate.webapp.rest.dto;

import java.util.Objects;

public class ProcessInstanceReferenceDto {
    private String instanceId;
    private String processDefinitionId;
    private String processDefinitionName;

    public String getInstanceId() {
        return this.instanceId;
    }

    public ProcessInstanceReferenceDto setInstanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    public String getProcessDefinitionId() {
        return this.processDefinitionId;
    }

    public ProcessInstanceReferenceDto setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
        return this;
    }

    public String getProcessDefinitionName() {
        return this.processDefinitionName;
    }

    public ProcessInstanceReferenceDto setProcessDefinitionName(String processDefinitionName) {
        this.processDefinitionName = processDefinitionName;
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
        ProcessInstanceReferenceDto that = (ProcessInstanceReferenceDto)o;
        return Objects.equals(this.instanceId, that.instanceId) && Objects.equals(this.processDefinitionId, that.processDefinitionId) && Objects.equals(this.processDefinitionName, that.processDefinitionName);
    }

    public int hashCode() {
        return Objects.hash(this.instanceId, this.processDefinitionId, this.processDefinitionName);
    }
}
