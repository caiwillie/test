/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.rest.dto.activity.FlowNodeInstanceDto
 */
package io.camunda.operate.webapp.rest.dto.activity;

import io.camunda.operate.webapp.rest.dto.activity.FlowNodeInstanceDto;
import java.util.List;
import java.util.Objects;

public class FlowNodeInstanceResponseDto {
    private Boolean isRunning;
    private List<FlowNodeInstanceDto> children;

    public FlowNodeInstanceResponseDto() {
    }

    public FlowNodeInstanceResponseDto(Boolean running, List<FlowNodeInstanceDto> children) {
        this.isRunning = running;
        this.children = children;
    }

    public List<FlowNodeInstanceDto> getChildren() {
        return this.children;
    }

    public FlowNodeInstanceResponseDto setChildren(List<FlowNodeInstanceDto> children) {
        this.children = children;
        return this;
    }

    public Boolean getRunning() {
        return this.isRunning;
    }

    public FlowNodeInstanceResponseDto setRunning(Boolean running) {
        this.isRunning = running;
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
        FlowNodeInstanceResponseDto that = (FlowNodeInstanceResponseDto)o;
        return Objects.equals(this.isRunning, that.isRunning) && Objects.equals(this.children, that.children);
    }

    public int hashCode() {
        return Objects.hash(this.isRunning, this.children);
    }
}
