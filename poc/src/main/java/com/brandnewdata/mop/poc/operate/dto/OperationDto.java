package com.brandnewdata.mop.poc.operate.dto;


import com.brandnewdata.mop.poc.operate.po.OperationPo;
import com.brandnewdata.mop.poc.operate.po.OperationState;
import com.brandnewdata.mop.poc.operate.po.OperationType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OperationDto implements FromOneEntity<OperationDto, OperationPo> {

    private String id;

    private String batchOperationId;

    private OperationType type;

    private OperationState state;

    private String errorMessage;

    @Override
    public OperationDto from(OperationPo entity) {
        this.setId(entity.getId());
        this.setType(entity.getType());
        this.setState(entity.getState());
        this.setErrorMessage(entity.getErrorMessage());
        this.setBatchOperationId(entity.getBatchOperationId());
        return this;
    }
}
