/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.FlowNodeInstanceEntity
 *  io.camunda.operate.entities.FlowNodeState
 *  io.camunda.operate.entities.FlowNodeType
 *  io.camunda.operate.webapp.rest.dto.CreatableFromEntity
 *  io.camunda.operate.webapp.rest.dto.activity.FlowNodeStateDto
 */
package io.camunda.operate.webapp.rest.dto.activity;

import io.camunda.operate.entities.FlowNodeInstanceEntity;
import io.camunda.operate.entities.FlowNodeState;
import io.camunda.operate.entities.FlowNodeType;
import io.camunda.operate.webapp.rest.dto.CreatableFromEntity;
import io.camunda.operate.webapp.rest.dto.activity.FlowNodeStateDto;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Objects;

public class FlowNodeInstanceDto
implements CreatableFromEntity<FlowNodeInstanceDto, FlowNodeInstanceEntity> {
    private String id;
    private FlowNodeType type;
    private FlowNodeStateDto state;
    private String flowNodeId;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private String treePath;
    private Object[] sortValues;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FlowNodeStateDto getState() {
        return this.state;
    }

    public void setState(FlowNodeStateDto state) {
        this.state = state;
    }

    public String getFlowNodeId() {
        return this.flowNodeId;
    }

    public void setFlowNodeId(String flowNodeId) {
        this.flowNodeId = flowNodeId;
    }

    public OffsetDateTime getStartDate() {
        return this.startDate;
    }

    public void setStartDate(OffsetDateTime startDate) {
        this.startDate = startDate;
    }

    public OffsetDateTime getEndDate() {
        return this.endDate;
    }

    public void setEndDate(OffsetDateTime endDate) {
        this.endDate = endDate;
    }

    public FlowNodeType getType() {
        return this.type;
    }

    public void setType(FlowNodeType type) {
        this.type = type;
    }

    public String getTreePath() {
        return this.treePath;
    }

    public void setTreePath(String treePath) {
        this.treePath = treePath;
    }

    public Object[] getSortValues() {
        return this.sortValues;
    }

    public void setSortValues(Object[] sortValues) {
        this.sortValues = sortValues;
    }

    public FlowNodeInstanceDto fillFrom(FlowNodeInstanceEntity flowNodeInstanceEntity) {
        this.setId(flowNodeInstanceEntity.getId());
        this.setFlowNodeId(flowNodeInstanceEntity.getFlowNodeId());
        this.setStartDate(flowNodeInstanceEntity.getStartDate());
        this.setEndDate(flowNodeInstanceEntity.getEndDate());
        if (flowNodeInstanceEntity.getState() == FlowNodeState.ACTIVE && flowNodeInstanceEntity.isIncident()) {
            this.setState(FlowNodeStateDto.INCIDENT);
        } else {
            this.setState(FlowNodeStateDto.getState((FlowNodeState)flowNodeInstanceEntity.getState()));
        }
        this.setType(flowNodeInstanceEntity.getType());
        this.setSortValues(flowNodeInstanceEntity.getSortValues());
        this.setTreePath(flowNodeInstanceEntity.getTreePath());
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
        FlowNodeInstanceDto that = (FlowNodeInstanceDto)o;
        return Objects.equals(this.id, that.id) && this.type == that.type && this.state == that.state && Objects.equals(this.flowNodeId, that.flowNodeId) && Objects.equals(this.startDate, that.startDate) && Objects.equals(this.endDate, that.endDate) && Objects.equals(this.treePath, that.treePath) && Arrays.equals(this.sortValues, that.sortValues);
    }

    public int hashCode() {
        int result = Objects.hash(this.id, this.type, this.state, this.flowNodeId, this.startDate, this.endDate, this.treePath);
        result = 31 * result + Arrays.hashCode(this.sortValues);
        return result;
    }
}
