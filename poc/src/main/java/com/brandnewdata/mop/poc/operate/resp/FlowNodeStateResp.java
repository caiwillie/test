package com.brandnewdata.mop.poc.operate.resp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlowNodeStateResp {

    /**
     * 流程实例id
     */
    private String processInstanceId;

    /**
     * 节点id
     */
    private String flowNodeId;

    /**
     * 节点状态
     *
     * ACTIVE 活动，
     * INCIDENT 异常，
     * COMPLETED 完成，
     * TERMINATED 取消
     */
    private String state;
}
