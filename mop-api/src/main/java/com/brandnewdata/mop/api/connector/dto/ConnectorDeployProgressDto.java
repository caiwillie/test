package com.brandnewdata.mop.api.connector.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ConnectorDeployProgressDto {
    private Map<String, ConnectorProcessDeployStatusDto> triggerDeployStatusMap;

    private Map<String, ConnectorProcessDeployStatusDto> operateDeployStatusMap;
}