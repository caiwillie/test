package com.brandnewdata.mop.poc.operate.po;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class IncidentPo extends OperateZeebePo<IncidentPo> {

    private ErrorType errorType;

    private String errorMessage;

    private Integer errorMessageHash;

    private IncidentState state;

    private String flowNodeId;

    private Long flowNodeInstanceKey;

    private Long jobKey;

    private Long processInstanceKey;

    private OffsetDateTime creationTime;

    private Long processDefinitionKey;

    private String treePath;

    private boolean pending = true;
}
