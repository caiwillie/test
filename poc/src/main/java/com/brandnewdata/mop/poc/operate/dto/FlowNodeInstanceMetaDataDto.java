package com.brandnewdata.mop.poc.operate.dto;

import com.brandnewdata.mop.poc.operate.po.EventMetadataPo;
import com.brandnewdata.mop.poc.operate.po.EventPo;
import com.brandnewdata.mop.poc.operate.po.FlowNodeInstancePo;
import com.brandnewdata.mop.poc.operate.po.FlowNodeType;
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

    public FlowNodeInstanceMetaDataDto fromEntity(FlowNodeInstancePo flowNodeInstancePo, EventPo eventEntity) {
        this.setProcessInstanceId(String.valueOf(flowNodeInstancePo.getProcessInstanceKey()));
        this.setFlowNodeInstanceId(flowNodeInstancePo.getId());
        this.setFlowNodeId(flowNodeInstancePo.getFlowNodeId());
        this.setFlowNodeType(flowNodeInstancePo.getType());
        this.setStartDate(Optional.ofNullable(flowNodeInstancePo.getStartDate()).map(OffsetDateTime::toLocalDateTime).orElse(null));
        this.setEndDate(Optional.ofNullable(flowNodeInstancePo.getEndDate()).map(OffsetDateTime::toLocalDateTime).orElse(null));

        EventMetadataPo eventMetadataPo = eventEntity.getMetadata();
        if (eventMetadataPo == null) return this;
        this.setJobCustomHeaders(eventMetadataPo.getJobCustomHeaders());
        this.setJobDeadline(eventMetadataPo.getJobDeadline());
        this.setJobRetries(eventMetadataPo.getJobRetries());
        this.setJobType(eventMetadataPo.getJobType());
        this.setJobWorker(eventMetadataPo.getJobWorker());
        return this;
    }
}
