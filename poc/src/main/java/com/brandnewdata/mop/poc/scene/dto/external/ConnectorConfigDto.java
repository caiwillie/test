package com.brandnewdata.mop.poc.scene.dto.external;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConnectorConfigDto {
    private String connectorGroup;
    private String connectorId;
    private String connectorVersion;
    private String connectorName;
    private String connectorIcon;
    private String configId;
    private String configName;
}
