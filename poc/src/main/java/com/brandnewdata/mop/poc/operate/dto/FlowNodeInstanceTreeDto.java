package com.brandnewdata.mop.poc.operate.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FlowNodeInstanceTreeDto {

    private boolean incident;

    private List<FlowNodeInstanceDto> list;

}
