package com.brandnewdata.mop.poc.bff.model.sceneOperate.condition;

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
    private int version;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

}
