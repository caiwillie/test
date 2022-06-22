/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.FlowNodeType
 */
package io.camunda.operate.webapp.rest.dto.metadata;

import io.camunda.operate.entities.FlowNodeType;
import java.util.Objects;

public class FlowNodeMetadataRequestDto {
    private String flowNodeId;
    private String flowNodeInstanceId;
    private FlowNodeType flowNodeType;

    public FlowNodeMetadataRequestDto() {
    }

    public FlowNodeMetadataRequestDto(String flowNodeId, String flowNodeInstanceId, FlowNodeType flowNodeType) {
        this.flowNodeId = flowNodeId;
        this.flowNodeInstanceId = flowNodeInstanceId;
        this.flowNodeType = flowNodeType;
    }

    public String getFlowNodeId() {
        return this.flowNodeId;
    }

    public FlowNodeMetadataRequestDto setFlowNodeId(String flowNodeId) {
        this.flowNodeId = flowNodeId;
        return this;
    }

    public String getFlowNodeInstanceId() {
        return this.flowNodeInstanceId;
    }

    public FlowNodeMetadataRequestDto setFlowNodeInstanceId(String flowNodeInstanceId) {
        this.flowNodeInstanceId = flowNodeInstanceId;
        return this;
    }

    public FlowNodeType getFlowNodeType() {
        return this.flowNodeType;
    }

    public FlowNodeMetadataRequestDto setFlowNodeType(FlowNodeType flowNodeType) {
        this.flowNodeType = flowNodeType;
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
        FlowNodeMetadataRequestDto that = (FlowNodeMetadataRequestDto)o;
        return Objects.equals(this.flowNodeId, that.flowNodeId) && Objects.equals(this.flowNodeInstanceId, that.flowNodeInstanceId) && this.flowNodeType == that.flowNodeType;
    }

    public int hashCode() {
        return Objects.hash(this.flowNodeId, this.flowNodeInstanceId, this.flowNodeType);
    }

    public String toString() {
        return "FlowNodeMetadataRequestDto{flowNodeId='" + this.flowNodeId + "', flowNodeInstanceId='" + this.flowNodeInstanceId + "', flowNodeType=" + this.flowNodeType + "}";
    }
}
