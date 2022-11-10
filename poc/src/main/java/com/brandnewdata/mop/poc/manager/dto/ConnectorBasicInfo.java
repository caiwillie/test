package com.brandnewdata.mop.poc.manager.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConnectorBasicInfo {
    private String connectorGroup;
    private String connectorId;
    private String connectorName;
    private String connectorVersion;
    private String connectorSmallIcon;
}
