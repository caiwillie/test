package com.brandnewdata.mop.poc.bff.vo.scene;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DebugProcessInstanceVo {

    /**
     * 流程实例 id
     */
    private String instanceId;

    /**
     * 部署id
     */
    private Long snapshotDeployId;

    /**
     * 环境id
     */
    private Long envId;

    /**
     * 流程 id
     */
    private String processId;

    /**
     * 流程名称
     */
    private String processName;

    /**
     * 运行状态：ACTIVE 运行，INCIDENT 异常，COMPLETED 完成，CANCELED 取消
     */
    private String state;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

}
