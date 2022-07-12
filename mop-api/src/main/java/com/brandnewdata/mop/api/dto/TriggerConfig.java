package com.brandnewdata.mop.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class TriggerConfig {

    /**
     * 触发器名称
     */
    private String triggerName;

    /**
     * 监听配置
     */
    private List<RequestParamConfig> requestParamConfigs;
}
