package com.brandnewdata.mop.poc.bff.vo.operate.process;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SequenceFlowVo {

    /**
     * 流程实例id
     */
    private String processInstanceId;

    /**
     * 节点id
     */
    private String sequenceFlowId;
}
