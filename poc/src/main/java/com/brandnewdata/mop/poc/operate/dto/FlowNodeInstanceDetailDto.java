package com.brandnewdata.mop.poc.operate.dto;

import lombok.Data;

@Data
public class FlowNodeInstanceDetailDto {

    /**
     * 说明当前节点是否重复
     */
    private boolean repeated;

    /**
     * 当前节点的flowNodeId
     */
    private String flowNodeId;

    /**
     * 当前节点的flowNodeType
     */
    private String flowNodeType;

    /**
     * 当前节点重复运行的实例数
     */
    private int instanceCount;

    /**
     * 基本信息
     */
    private FlowNodeInstanceMetaDataDto meteData;

    /**
     * 异常信息
     */
    private IncidentDto incident;

}
