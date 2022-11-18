package com.brandnewdata.mop.poc.operate.dto;

import com.brandnewdata.mop.poc.operate.entity.EventEntity;
import com.brandnewdata.mop.poc.operate.entity.EventMetadataEntity;
import com.brandnewdata.mop.poc.operate.entity.FlowNodeInstanceEntity;
import com.brandnewdata.mop.poc.operate.entity.FlowNodeType;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

@Data
public class FlowNodeInstanceMetaDataDto {
    private String processInstanceId;
    private String flowNodeId;
    private String flowNodeInstanceId;
    private FlowNodeType flowNodeType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
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

    public FlowNodeInstanceMetaDataDto fromEntity(FlowNodeInstanceEntity flowNodeInstanceEntity, EventEntity eventEntity) {
        this.setProcessInstanceId(String.valueOf(flowNodeInstanceEntity.getProcessInstanceKey()));
        this.setFlowNodeInstanceId(flowNodeInstanceEntity.getId());
        this.setFlowNodeId(flowNodeInstanceEntity.getFlowNodeId());
        this.setFlowNodeType(flowNodeInstanceEntity.getType());
        this.setStartDate(Optional.ofNullable(flowNodeInstanceEntity.getStartDate()).map(OffsetDateTime::toLocalDateTime).orElse(null));
        this.setEndDate(Optional.ofNullable(flowNodeInstanceEntity.getEndDate()).map(OffsetDateTime::toLocalDateTime).orElse(null));

        EventMetadataEntity eventMetadataEntity = eventEntity.getMetadata();
        if (eventMetadataEntity == null) return this;
        this.setJobCustomHeaders(eventMetadataEntity.getJobCustomHeaders());
        this.setJobDeadline(eventMetadataEntity.getJobDeadline());
        this.setJobRetries(eventMetadataEntity.getJobRetries());
        this.setJobType(eventMetadataEntity.getJobType());
        this.setJobWorker(eventMetadataEntity.getJobWorker());
        return this;
    }
}
