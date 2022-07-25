package com.brandnewdata.mop.poc.modeler.dto;

import lombok.Data;

@Data
public class TriggerOrOperate {
    private String groupId;

    private String connectorId;

    private String triggerOrOperateId;

    private String version;
}
