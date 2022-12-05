package com.brandnewdata.mop.api.connector.dto;

import lombok.Data;

import java.util.List;

@Data
public class TriggerConfig {

    /**
     * 触发器名称
     */
    private String triggerName;

    /**
     * 触发器xml
     */
    private String editorXML;

    /**
     * 监听配置
     */
    private List<RequestParamConfig> requestParamConfigs;
}
