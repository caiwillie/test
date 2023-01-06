package com.brandnewdata.mop.poc.scene.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ProcessDeployProgressDto {
    /**
     * 0 待部署，1 部署成功，2 部署异常
     */
    private int status;

    /**
     * 异常信息
     */
    private Map<String, String> errorMessageMap;

    public ProcessDeployProgressDto(int status, Map<String, String> errorMessageMap) {
        this.status = status;
        this.errorMessageMap = errorMessageMap;
    }
}
