package com.brandnewdata.mop.api;

import lombok.Data;

import java.util.Map;

/**
 * 启动消息实体
 */

@Data
public class StartMessage {
    /**
     * 消息协议类型
     */
    private String protocol;

    /**
     * 流程 modelKey
     */
    private String modelKey;

    /**
     * 消息内容
     */
    private Map<String, Object> content;
}
