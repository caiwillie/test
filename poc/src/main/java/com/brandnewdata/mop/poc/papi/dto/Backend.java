package com.brandnewdata.mop.poc.papi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Backend {

    private int type;

    private Object data;

    public Backend(int type, Object data) {
        this.type = type;
        this.data = data;
    }
}
