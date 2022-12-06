package com.brandnewdata.mop.poc.operate.dto;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.brandnewdata.mop.poc.operate.po.IncidentPo;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;

@Getter
@Setter
public class IncidentDto {

    public static final String FALLBACK_PROCESS_DEFINITION_NAME = "Unknown process";

    private String id;

    private ErrorTypeDto errorType;

    private String errorMessage;

    private String flowNodeId;

    private String flowNodeInstanceId;

    private String jobId;

    private LocalDateTime creationTime;

    private boolean hasActiveOperation = false;

    private LocalDateTime lastOperation;

    private ProcessInstanceReferenceDto rootCauseInstance;

    private DecisionInstanceReferenceDto rootCauseDecision;


    public IncidentDto fromEntity(IncidentPo incidentPo) {
        this.setId(incidentPo.getId());
        this.setFlowNodeId(incidentPo.getFlowNodeId());
        this.setFlowNodeInstanceId(Optional.ofNullable(incidentPo.getFlowNodeInstanceKey()).map(String::valueOf).orElse(null));
        this.setErrorMessage(incidentPo.getErrorMessage());
        this.setErrorType(new ErrorTypeDto().from(incidentPo.getErrorType()));
        this.setJobId(Optional.ofNullable(incidentPo.getJobKey()).map(String::valueOf).orElse(null));
        this.setCreationTime(Optional.ofNullable(incidentPo.getCreationTime())
                .map(offsetDateTime -> LocalDateTimeUtil.of(offsetDateTime.toInstant())).orElse(null));
        return this;
    }

}
