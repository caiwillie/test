package com.brandnewdata.mop.api.connector.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ConnectorDeployProgressDto {
    private Map<String, DeployProgressDto> triggerProgressMap;

    private Map<String, DeployProgressDto> operateProgressMap;
}
