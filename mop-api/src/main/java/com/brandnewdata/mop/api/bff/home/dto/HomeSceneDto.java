package com.brandnewdata.mop.api.bff.home.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HomeSceneDto {

    /**
     * 场景名称
     */
    private String name;

    /**
     * 场景状态
     */
    private String status;

    /**
     * 版本
     */
    private String version;

    /**
     * 环境
     */
    private String env;

    /**
     * 更新时间
     */
    private String updateTime;

    /**
     * 场景7日运行数量-总数
     */
    private int processInstanceCount;

    /**
     * 场景7日运行数量-失败数
     */
    private int processInstanceFailCount;
}
