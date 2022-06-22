/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.EventEntity
 *  io.camunda.operate.entities.EventMetadataEntity
 *  io.camunda.operate.entities.FlowNodeInstanceEntity
 *  io.camunda.operate.entities.FlowNodeType
 */
package io.camunda.operate.webapp.rest.dto.metadata;

import io.camunda.operate.entities.EventEntity;
import io.camunda.operate.entities.EventMetadataEntity;
import io.camunda.operate.entities.FlowNodeInstanceEntity;
import io.camunda.operate.entities.FlowNodeType;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;

public class FlowNodeInstanceMetadataDto {
    private String flowNodeId;
    private String flowNodeInstanceId;
    private FlowNodeType flowNodeType;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private String calledProcessInstanceId;
    private String calledProcessDefinitionName;
    private String calledDecisionInstanceId;
    private String calledDecisionDefinitionName;
    private String eventId;
    private String jobType;
    private Integer jobRetries;
    private String jobWorker;
    private OffsetDateTime jobDeadline;
    private Map<String, String> jobCustomHeaders;

    public String getFlowNodeId() {
        return this.flowNodeId;
    }

    public FlowNodeInstanceMetadataDto setFlowNodeId(String flowNodeId) {
        this.flowNodeId = flowNodeId;
        return this;
    }

    public String getFlowNodeInstanceId() {
        return this.flowNodeInstanceId;
    }

    public FlowNodeInstanceMetadataDto setFlowNodeInstanceId(String flowNodeInstanceId) {
        this.flowNodeInstanceId = flowNodeInstanceId;
        return this;
    }

    public FlowNodeType getFlowNodeType() {
        return this.flowNodeType;
    }

    public FlowNodeInstanceMetadataDto setFlowNodeType(FlowNodeType flowNodeType) {
        this.flowNodeType = flowNodeType;
        return this;
    }

    public OffsetDateTime getStartDate() {
        return this.startDate;
    }

    public FlowNodeInstanceMetadataDto setStartDate(OffsetDateTime startDate) {
        this.startDate = startDate;
        return this;
    }

    public OffsetDateTime getEndDate() {
        return this.endDate;
    }

    public FlowNodeInstanceMetadataDto setEndDate(OffsetDateTime endDate) {
        this.endDate = endDate;
        return this;
    }

    public String getCalledProcessInstanceId() {
        return this.calledProcessInstanceId;
    }

    public FlowNodeInstanceMetadataDto setCalledProcessInstanceId(String calledProcessInstanceId) {
        this.calledProcessInstanceId = calledProcessInstanceId;
        return this;
    }

    public String getCalledProcessDefinitionName() {
        return this.calledProcessDefinitionName;
    }

    public FlowNodeInstanceMetadataDto setCalledProcessDefinitionName(String calledProcessDefinitionName) {
        this.calledProcessDefinitionName = calledProcessDefinitionName;
        return this;
    }

    public String getCalledDecisionInstanceId() {
        return this.calledDecisionInstanceId;
    }

    public FlowNodeInstanceMetadataDto setCalledDecisionInstanceId(String calledDecisionInstanceId) {
        this.calledDecisionInstanceId = calledDecisionInstanceId;
        return this;
    }

    public String getCalledDecisionDefinitionName() {
        return this.calledDecisionDefinitionName;
    }

    public FlowNodeInstanceMetadataDto setCalledDecisionDefinitionName(String calledDecisionDefinitionName) {
        this.calledDecisionDefinitionName = calledDecisionDefinitionName;
        return this;
    }

    public String getEventId() {
        return this.eventId;
    }

    public FlowNodeInstanceMetadataDto setEventId(String eventId) {
        this.eventId = eventId;
        return this;
    }

    public String getJobType() {
        return this.jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public Integer getJobRetries() {
        return this.jobRetries;
    }

    public void setJobRetries(Integer jobRetries) {
        this.jobRetries = jobRetries;
    }

    public String getJobWorker() {
        return this.jobWorker;
    }

    public void setJobWorker(String jobWorker) {
        this.jobWorker = jobWorker;
    }

    public OffsetDateTime getJobDeadline() {
        return this.jobDeadline;
    }

    public void setJobDeadline(OffsetDateTime jobDeadline) {
        this.jobDeadline = jobDeadline;
    }

    public Map<String, String> getJobCustomHeaders() {
        return this.jobCustomHeaders;
    }

    public void setJobCustomHeaders(Map<String, String> jobCustomHeaders) {
        this.jobCustomHeaders = jobCustomHeaders;
    }

    public static FlowNodeInstanceMetadataDto createFrom(FlowNodeInstanceEntity flowNodeInstance, EventEntity eventEntity, String calledProcessInstanceId, String calledProcessDefinitionName, String calledDecisionInstanceId, String calledDecisionDefinitionName) {
        FlowNodeInstanceMetadataDto metadataDto = new FlowNodeInstanceMetadataDto();
        metadataDto.setFlowNodeInstanceId(flowNodeInstance.getId());
        metadataDto.setFlowNodeId(flowNodeInstance.getFlowNodeId());
        metadataDto.setFlowNodeType(flowNodeInstance.getType());
        metadataDto.setStartDate(flowNodeInstance.getStartDate());
        metadataDto.setEndDate(flowNodeInstance.getEndDate());
        if (calledProcessInstanceId != null) {
            metadataDto.setCalledProcessInstanceId(calledProcessInstanceId);
        }
        if (calledProcessDefinitionName != null) {
            metadataDto.setCalledProcessDefinitionName(calledProcessDefinitionName);
        }
        if (calledDecisionInstanceId != null) {
            metadataDto.setCalledDecisionInstanceId(calledDecisionInstanceId);
        }
        if (calledDecisionDefinitionName != null) {
            metadataDto.setCalledDecisionDefinitionName(calledDecisionDefinitionName);
        }
        metadataDto.setEventId(eventEntity.getId());
        EventMetadataEntity eventMetadataEntity = eventEntity.getMetadata();
        if (eventMetadataEntity == null) return metadataDto;
        metadataDto.setJobCustomHeaders(eventMetadataEntity.getJobCustomHeaders());
        metadataDto.setJobDeadline(eventMetadataEntity.getJobDeadline());
        metadataDto.setJobRetries(eventMetadataEntity.getJobRetries());
        metadataDto.setJobType(eventMetadataEntity.getJobType());
        metadataDto.setJobWorker(eventMetadataEntity.getJobWorker());
        return metadataDto;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        FlowNodeInstanceMetadataDto that = (FlowNodeInstanceMetadataDto)o;
        return Objects.equals(this.flowNodeId, that.flowNodeId) && Objects.equals(this.flowNodeInstanceId, that.flowNodeInstanceId) && this.flowNodeType == that.flowNodeType && Objects.equals(this.startDate, that.startDate) && Objects.equals(this.endDate, that.endDate) && Objects.equals(this.calledProcessInstanceId, that.calledProcessInstanceId) && Objects.equals(this.calledProcessDefinitionName, that.calledProcessDefinitionName) && Objects.equals(this.calledDecisionInstanceId, that.calledDecisionInstanceId) && Objects.equals(this.calledDecisionDefinitionName, that.calledDecisionDefinitionName) && Objects.equals(this.eventId, that.eventId) && Objects.equals(this.jobType, that.jobType) && Objects.equals(this.jobRetries, that.jobRetries) && Objects.equals(this.jobWorker, that.jobWorker) && Objects.equals(this.jobDeadline, that.jobDeadline) && Objects.equals(this.jobCustomHeaders, that.jobCustomHeaders);
    }

    public int hashCode() {
        return Objects.hash(this.flowNodeId, this.flowNodeInstanceId, this.flowNodeType, this.startDate, this.endDate, this.calledProcessInstanceId, this.calledProcessDefinitionName, this.calledDecisionInstanceId, this.calledDecisionDefinitionName, this.eventId, this.jobType, this.jobRetries, this.jobWorker, this.jobDeadline, this.jobCustomHeaders);
    }

    public String toString() {
        return "FlowNodeInstanceMetadataDto{flowNodeId='" + this.flowNodeId + "', flowNodeInstanceId='" + this.flowNodeInstanceId + "', flowNodeType=" + this.flowNodeType + ", startDate=" + this.startDate + ", endDate=" + this.endDate + ", calledProcessInstanceId='" + this.calledProcessInstanceId + "', calledProcessDefinitionName='" + this.calledProcessDefinitionName + "', calledDecisionInstanceId='" + this.calledDecisionInstanceId + "', calledDecisionDefinitionName='" + this.calledDecisionDefinitionName + "', eventId='" + this.eventId + "', jobType='" + this.jobType + "', jobRetries=" + this.jobRetries + ", jobWorker='" + this.jobWorker + "', jobDeadline=" + this.jobDeadline + ", jobCustomHeaders=" + this.jobCustomHeaders + "}";
    }
}
