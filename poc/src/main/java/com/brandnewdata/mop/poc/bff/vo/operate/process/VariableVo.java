package com.brandnewdata.mop.poc.bff.vo.operate.process;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VariableVo {
    private String id;

    private String name;

    private String value;

    private boolean isPreview;

    private boolean hasActiveOperation = false;

    private boolean isFirst = false;

    private String[] sortValues;
}
