package com.brandnewdata.mop.poc.bff.model.sceneOperate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProcessInstance {

    /**
     * 流程实例 id
     */
    private String instanceId;

    /**
     * 场景id
     */
    private Long sceneId;

    /**
     * 场景名称
     */
    private String sceneName;

    /**
     * 部署id
     */
    private Long deployId;

    /**
     * 流程 id
     */
    private String processId;

    /**
     * 流程名称
     */
    private String processName;

    /**
     * 流程版本
     */
    private int version;

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
