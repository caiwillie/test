package com.brandnewdata.mop.poc.bff.vo.operate.charts;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Series {
    private String name;
    private Object[] data;

    public Series(String name, Object[] data) {
        this.name = name;
        this.data = data;
    }
}
