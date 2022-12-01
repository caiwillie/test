package com.brandnewdata.mop.poc.bff.vo.scene.operate;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class OperateProcessInstanceVo {

    /**
     * 环境id
     */
    private Long envId;

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
     * 版本id
     */
    private Long versionId;

    /**
     * 版本名称
     */
    private String versionName;

    /**
     * 流程 id
     */
    private String processId;

    /**
     * 流程名称
     */
    private String processName;

    /**
     * 部署id
     */
    private Long releaseDeployId;

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
