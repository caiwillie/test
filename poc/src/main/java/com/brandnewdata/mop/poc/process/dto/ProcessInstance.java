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
    private String version;

    /**
     * 流程实例 id
     */
    private String instanceId;

    /**
     * 父级流程实例 id
     */
    private String parentInstanceId;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;
}
