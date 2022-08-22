package com.brandnewdata.mop.poc.operate.dto;


import com.brandnewdata.mop.poc.operate.entity.OperationState;
import com.brandnewdata.mop.poc.operate.entity.OperationType;

public class OperationDto {

    private String id;

    private String batchOperationId;

    private OperationType type;

    private OperationState state;

    private String errorMessage;

}
