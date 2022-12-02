package com.brandnewdata.mop.poc.bff.vo.scene.operate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SceneDeployFilter {

    /**
     * 分页页码
     */
    private Integer pageNum;

    /**
     * 分页大小
     */
    private Integer pageSize;

    /**
     * 环境id
     */
    private Long envId;

    /**
     * 场景id
     */
    private Long sceneId;

    /**
     * 版本
     */
    private Long versionId;

    /**
     * 流程id
     */
    private String processId;

    /**
     * 开始时间 yyyy-MM-dd HH:mm:ss
     */
    private String startTime;

    /**
     * 结束时间 yyyy-MM-dd HH:mm:ss
     */
    private String endTime;
}
