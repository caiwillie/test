package com.brandnewdata.mop.poc.process.manager.dto;

import lombok.Data;

@Data
public class TriggerInfo {

    /**
     * xml
     */
    private String processEditing;

    /**
     * 触发器 id
     */
    private String triggerName;

    /**
     * 触发器名称
     */
    private String triggerShowName;

}
