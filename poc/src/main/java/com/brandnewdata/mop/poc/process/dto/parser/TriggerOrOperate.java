package com.brandnewdata.mop.poc.process.dto.parser;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

@Data
public class TriggerOrOperate {
    private String groupId;

    private String connectorId;

    private String triggerOrOperateId;

    private String version;

    public String getFullId() {
        return StrUtil.format("{}:{}.{}:{}", groupId, connectorId, triggerOrOperateId, version);
    }
}
