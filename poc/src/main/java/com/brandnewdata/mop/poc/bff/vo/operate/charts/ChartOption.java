package com.brandnewdata.mop.poc.bff.vo.operate.charts;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChartOption {
    private Object[] category;
    private Series[] series;
}
