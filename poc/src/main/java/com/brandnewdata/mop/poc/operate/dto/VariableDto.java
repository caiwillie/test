package com.brandnewdata.mop.poc.operate.dto;

import com.brandnewdata.mop.poc.operate.entity.VariableEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VariableDto extends OperateZeebeDto {

    private String id;

    private String name;

    private String value;

    private boolean isPreview;

    private boolean hasActiveOperation = false;

    private boolean isFirst = false;

    private String[] sortValues;
}
