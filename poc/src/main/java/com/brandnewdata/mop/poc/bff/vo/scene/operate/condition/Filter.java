package com.brandnewdata.mop.poc.bff.vo.scene.operate.condition;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Filter {

    /**
     * 场景id
     */
    private Long sceneId;

    /**
     * 流程id
     */
    private String processId;

    /**
     * 版本
     */
    private Integer version;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 分页页码
     */
    private Integer pageNum;

    /**
     * 分页大小
     */
    private Integer pageSize;

}
