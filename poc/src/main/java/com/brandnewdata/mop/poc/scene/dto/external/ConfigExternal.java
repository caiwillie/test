package com.brandnewdata.mop.poc.scene.dto.external;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfigExternal {
    private String connectorType;
    private String connectorName;
    private String connectorVersion;
    private String connectorIcon;
    private String configId;
    private String newConfigId;
}
