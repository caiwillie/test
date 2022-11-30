package com.brandnewdata.mop.poc.operate.dto;

import com.brandnewdata.mop.poc.operate.po.ErrorType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorTypeDto implements FromOneEntity<ErrorTypeDto, ErrorType> {

    private String id;

    private String name;

    @Override
    public ErrorTypeDto from(ErrorType entity) {
        this.setId(entity.name());
        this.setName(entity.getTitle());
        return this;
    }
}
