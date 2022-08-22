package com.brandnewdata.mop.poc.operate.dto;

import com.brandnewdata.mop.poc.operate.entity.ErrorType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorTypeDto implements FromOneEntity<ErrorTypeDto, ErrorType> {

    private String id;

    private String name;

    @Override
    public ErrorTypeDto fromEntity(ErrorType entity) {
        this.setId(entity.name());
        this.setName(entity.getTitle());
        return this;
    }
}
