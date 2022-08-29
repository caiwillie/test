package com.brandnewdata.mop.poc.operate.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SequenceFlowEntity extends OperateEntity<SequenceFlowEntity> {

    private Long processInstanceKey;

    private String activityId;
}
