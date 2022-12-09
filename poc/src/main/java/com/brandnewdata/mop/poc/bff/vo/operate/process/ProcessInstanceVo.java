package com.brandnewdata.mop.poc.bff.vo.operate.process;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProcessInstanceVo {
    /**
     * 流程实例 id
     */
    private String instanceId;

    /**
     * 流程 id
     */
    private String processId;

    /**
     * 父级流程实例 id
     */
    private String parentInstanceId;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 运行状态：ACTIVE 运行，INCIDENT 异常，COMPLETED 完成，CANCELED 取消
     */
    private String state;
}
