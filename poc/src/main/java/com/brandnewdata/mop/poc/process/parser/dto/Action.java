package com.brandnewdata.mop.poc.process.parser.dto;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

@Data
public class Action {
    private String connectorGroup;

    private String connectorId;

    private String connectorVersion;

    private String actionId;

    public String getFullId() {
        return StrUtil.format("{}:{}.{}:{}", connectorGroup, connectorId, actionId, connectorVersion);
    }
}
