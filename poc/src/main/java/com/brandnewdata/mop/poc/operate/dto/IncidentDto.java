package com.brandnewdata.mop.poc.operate.dto;

import java.time.OffsetDateTime;

public class IncidentDto {

    public static final String FALLBACK_PROCESS_DEFINITION_NAME = "Unknown process";
    private String id;
    private ErrorTypeDto errorType;
    private String errorMessage;
    private String flowNodeId;
    private String flowNodeInstanceId;
    private String jobId;
    private OffsetDateTime creationTime;
    private boolean hasActiveOperation = false;
    private OperationDto lastOperation;
    private ProcessInstanceReferenceDto rootCauseInstance;
    private DecisionInstanceReferenceDto rootCauseDecision;

}
