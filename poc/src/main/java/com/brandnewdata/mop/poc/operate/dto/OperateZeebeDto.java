package com.brandnewdata.mop.poc.operate.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class OperateZeebeDto extends OperateDto {

    private long key;

    private int partitionId;
}
