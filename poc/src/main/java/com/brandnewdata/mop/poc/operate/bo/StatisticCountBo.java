package com.brandnewdata.mop.poc.operate.bo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatisticCountBo {
    private int completedCount;
    private int activeCount;
    private int incidentCount;
    private int canceledCount;
}
