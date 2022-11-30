package com.brandnewdata.mop.poc.operate.converter;

import com.brandnewdata.mop.poc.operate.dto.VariableDto;
import com.brandnewdata.mop.poc.operate.entity.VariableEntity;

public class VariableDtoConverter {

    public static VariableDto createFrom(VariableEntity entity) {
        VariableDto dto = new VariableDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setPreview(entity.isPreview());
        dto.setValue(entity.getValue());
        return dto;
    }
}
