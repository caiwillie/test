package com.brandnewdata.mop.poc.operate.dto;


import com.brandnewdata.mop.poc.operate.entity.OperationEntity;
import com.brandnewdata.mop.poc.operate.entity.OperationState;
import com.brandnewdata.mop.poc.operate.entity.OperationType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OperationDto implements FromOneEntity<OperationDto, OperationEntity> {

    private String id;

    private String batchOperationId;

    private OperationType type;

    private OperationState state;

    private String errorMessage;

    @Override
    public OperationDto fromEntity(OperationEntity entity) {
        this.setId(entity.getId());
        this.setType(entity.getType());
        this.setState(entity.getState());
        this.setErrorMessage(entity.getErrorMessage());
        this.setBatchOperationId(entity.getBatchOperationId());
        return this;
    }
}
