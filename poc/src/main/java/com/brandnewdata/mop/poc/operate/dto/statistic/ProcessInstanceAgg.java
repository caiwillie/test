package com.brandnewdata.mop.poc.operate.dto.statistic;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ProcessInstanceAgg {
    private Long processInstanceKey;

    private LocalDate startDate;

    private String state;

    private Boolean incident;

    private Integer docCount;
}
