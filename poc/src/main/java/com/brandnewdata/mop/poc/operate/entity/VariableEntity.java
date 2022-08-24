package com.brandnewdata.mop.poc.operate.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VariableEntity extends OperateZeebeEntity<VariableEntity> {
    private String name;
    private String value;
    private String fullValue;
    private boolean isPreview;
    private Long scopeKey;
    private Long processInstanceKey;
    @JsonIgnore
    private Object[] sortValues;
}
