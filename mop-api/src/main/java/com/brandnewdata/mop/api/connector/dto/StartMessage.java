package com.brandnewdata.mop.api.connector.dto;

import lombok.Data;

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
    private String processId;

    /**
     * 消息内容
     */
    private String content;
}
