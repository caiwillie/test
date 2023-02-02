package com.brandnewdata.mop.poc.operate.dto.statistic;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProcessInstanceStateAgg {
    private String state;

    private Boolean incident;

    private Integer docCount;
}
