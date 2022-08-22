package com.brandnewdata.mop.poc.operate.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class OperationEntity extends OperateZeebeEntity<OperationEntity> {
    private Long processInstanceKey;
    private Long incidentKey;
    private Long scopeKey;
    private String variableName;
    private String variableValue;
    private OperationType type;
    private OffsetDateTime lockExpirationTime;
    private String lockOwner;
    private OperationState state;
    private String errorMessage;
    private String batchOperationId;
    private Long zeebeCommandKey;
    private String username;
}
