package com.brandnewdata.mop.poc.operate.po;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SequenceFlowPo extends OperatePo<SequenceFlowPo> {

    private Long processInstanceKey;

    private String activityId;
}
