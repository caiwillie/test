package com.brandnewdata.mop.api.connector.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ConnectorProcessDeployStatusDto {

    /**
     * 0 待部署，1 部署成功，2 部署异常
     */
    private int status;

    /**
     * 异常信息
     */
    private Map<String, String> errorMessageMap;

    public ConnectorProcessDeployStatusDto(int status, Map<String, String> errorMessageMap) {
        this.status = status;
        this.errorMessageMap = errorMessageMap;
    }
}
