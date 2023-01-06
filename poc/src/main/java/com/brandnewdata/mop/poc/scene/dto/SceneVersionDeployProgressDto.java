package com.brandnewdata.mop.poc.scene.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class SceneVersionDeployProgressDto {
    /**
     * 场景部署状态
     */
    private Map<String, ProcessDeployProgressDto> processDeployStatusMap;


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
