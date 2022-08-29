package com.brandnewdata.mop.poc.operate.resp;

import com.brandnewdata.mop.poc.operate.entity.SequenceFlowEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SequenceFlowResp {

    /**
     * 流程实例id
     */
    private String processInstanceId;

    /**
     * 节点id
     */
    private String sequenceFlowId;


    public SequenceFlowResp from(SequenceFlowEntity entity) {
        this.setProcessInstanceId(String.valueOf(entity.getProcessInstanceKey()));
        this.setSequenceFlowId(entity.getActivityId());
        return this;
    }
}
