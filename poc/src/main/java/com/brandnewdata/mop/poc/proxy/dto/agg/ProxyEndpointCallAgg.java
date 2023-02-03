package com.brandnewdata.mop.poc.proxy.dto.agg;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ProxyEndpointCallAgg {
    private Long endpointId;

    private LocalDate createDate;

    private String executeStatus;

    private Long rowCount;

    private Long timeConsumeSum;
}
