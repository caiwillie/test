package com.brandnewdata.mop.poc.operate.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VariablePo extends OperateZeebePo<VariablePo> {
    private String name;
    private String value;
    private String fullValue;
    private boolean isPreview;
    private Long scopeKey;
    private Long processInstanceKey;
    @JsonIgnore
    private Object[] sortValues;
}
