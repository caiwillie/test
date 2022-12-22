package com.brandnewdata.mop.api.connector.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProcessInstanceDto {

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
     * 运行状态：ACTIVE 运行，INCIDENT 异常，COMPLETED 完成，CANCELED 取消
     */
    private String state;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;
}
