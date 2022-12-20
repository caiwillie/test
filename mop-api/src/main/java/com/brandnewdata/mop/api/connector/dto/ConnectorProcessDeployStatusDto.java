package com.brandnewdata.mop.api.connector.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ConnectorProcessDeployStatusDto {

    private int status;

    private Map<String, String> messageMap;
}
