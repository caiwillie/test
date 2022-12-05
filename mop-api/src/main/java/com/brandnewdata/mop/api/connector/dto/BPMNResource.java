package com.brandnewdata.mop.api.connector.dto;

import lombok.Data;

/**
 * 流程资源实体
 */

@Data
public class BPMNResource {
    /**
     * 模型标识 (需要唯一)
     */
    private String modelKey;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 流程XML
     */
    private String editorXML;
}
