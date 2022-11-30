package com.brandnewdata.mop.poc.operate.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SequenceFlowDto {

    private String id;

    private Long processInstanceKey;

    private String activityId;
}
