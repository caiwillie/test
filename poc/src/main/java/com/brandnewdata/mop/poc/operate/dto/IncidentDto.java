package com.brandnewdata.mop.poc.operate.dto;

import com.brandnewdata.mop.poc.operate.entity.IncidentEntity;
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

    private ErrorTypeDTO errorType;

    private String errorMessage;

    private String flowNodeId;

    private String flowNodeInstanceId;

    private String jobId;

    private LocalDateTime creationTime;

    private boolean hasActiveOperation = false;

    private LocalDateTime lastOperation;

    private ProcessInstanceReferenceDto rootCauseInstance;

    private DecisionInstanceReferenceDTO rootCauseDecision;


    public IncidentDto fromEntity(IncidentEntity incidentEntity) {
        this.setId(incidentEntity.getId());
        this.setFlowNodeId(incidentEntity.getFlowNodeId());
        this.setFlowNodeInstanceId(Optional.ofNullable(incidentEntity.getFlowNodeInstanceKey()).map(String::valueOf).orElse(null));
        this.setErrorMessage(incidentEntity.getErrorMessage());
        this.setErrorType(new ErrorTypeDTO().from(incidentEntity.getErrorType()));
        this.setJobId(Optional.ofNullable(incidentEntity.getJobKey()).map(String::valueOf).orElse(null));
        this.setCreationTime(Optional.ofNullable(incidentEntity.getCreationTime()).map(OffsetDateTime::toLocalDateTime).orElse(null));
        return this;
    }

}
