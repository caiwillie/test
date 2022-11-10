package com.brandnewdata.mop.poc.process.parser.dto;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

@Data
public class Action {
    private String groupId;

    private String connectorId;

    private String actionId;

    private String version;

    public String getFullId() {
        return StrUtil.format("{}:{}.{}:{}", groupId, connectorId, actionId, version);
    }
}
