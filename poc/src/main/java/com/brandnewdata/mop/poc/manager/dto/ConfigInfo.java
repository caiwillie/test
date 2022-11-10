package com.brandnewdata.mop.poc.manager.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfigInfo {
    private Long id;
    private String connectorGroup;
    private String connectorId;
    private String connectorVersion;
    private String configName;
    private String configs;
}
