package com.brandnewdata.mop.api.dto;

import com.brandnewdata.mop.api.dto.RequestParamConfig;
import lombok.Data;

import java.util.List;

@Data
public class TriggerConfig {

    /**
     * 连接器id
     */
    private String id;

    /**
     * 监听配置
     */
    private List<RequestParamConfig> requestParamConfigs;
}
