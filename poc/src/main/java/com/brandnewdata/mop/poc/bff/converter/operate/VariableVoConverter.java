package com.brandnewdata.mop.poc.bff.converter.operate;

import com.brandnewdata.mop.poc.bff.vo.operate.process.VariableVo;
import com.brandnewdata.mop.poc.operate.dto.VariableDto;

public class VariableVoConverter {

    /*
    private String name;

    private String value;

    private boolean isPreview;

    private boolean hasActiveOperation = false;

    private boolean isFirst = false;

    private String[] sortValues;
    * */
    public static VariableVo createFrom(VariableDto dto) {
        VariableVo vo = new VariableVo();
        vo.setId(dto.getId());
        vo.setName(dto.getName());
        vo.setValue(dto.getValue());
        vo.setPreview(dto.isPreview());
        vo.setHasActiveOperation(dto.isHasActiveOperation());
        vo.setFirst(dto.isFirst());
        vo.setSortValues(dto.getSortValues());
        return vo;
    }
}
