package com.brandnewdata.mop.api.connector.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ConnectorDeployProgressDto {
    /**
     * 触发器部署状态
     */
    private Map<String, ConnectorProcessDeployStatusDto> triggerDeployStatusMap;

    /**
     * 操作部署状态
     */
    private Map<String, ConnectorProcessDeployStatusDto> operateDeployStatusMap;

    /**
     * 整体状态. 0 待部署，1 部署成功，2 部署异常
     */
    private int status;

    /**
     * 进度百分比
     */
    private double progressPercentage;

    /**
     * 整体异常信息
     */
    private String errorMessage;
}
