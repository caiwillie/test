package com.brandnewdata.mop.poc.process.dto;

import lombok.Data;

/**
 * 流程部署实体
 */
@Data
public class ProcessDeploy {

    /**
     * 部署 id
     */
    private Long id;

    /**
     * 流程 id
     */
    private String processId;

    /**
     * 流程名称
     */
    private String processName;

    /**
     * 部署 xml
     */
    private String xml;

    /**
     * 部署 版本
     */
    private int version;

    /**
     * 类型：1 场景，2 触发器，3 操作
     */
    private int type;

}
