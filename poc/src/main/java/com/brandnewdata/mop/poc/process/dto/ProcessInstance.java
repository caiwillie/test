package com.brandnewdata.mop.poc.process.dto;

import lombok.Data;

@Data
public class ProcessInstance {

    /**
     * 流程 id
     */
    private String processId;

    /**
     * 流程版本
     */
    private Long version;

    /**
     * 流程实例 id
     */
    private Long instanceId;

    /**
     * 父级流程实例 id
     */
    private Long parentInstanceId;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;
}