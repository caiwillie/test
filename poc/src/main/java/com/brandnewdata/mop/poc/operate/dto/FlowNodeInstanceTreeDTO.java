package com.brandnewdata.mop.poc.operate.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FlowNodeInstanceTreeDTO {

    private boolean incident;

    private List<FlowNodeInstanceDto> list;

}
