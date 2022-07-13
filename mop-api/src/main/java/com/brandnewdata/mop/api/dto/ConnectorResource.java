package com.brandnewdata.mop.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class ConnectorResource {

    /**
     * 触发器列表
     */
    private List<BPMNResource> triggers;

    /**
     * 操作列表
     */
    private List<BPMNResource> operates;

}
