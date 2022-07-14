package com.brandnewdata.mop.api.dto;

import lombok.Data;


/**
 * 监听配置
 *
 * @author caiwillie
 */
@Data
public class RequestParamConfig {

    /**
     * 配置名称
     */
    private String paramName;

    /**
     * 配置显示名称
     */
    private String paramShowName;

    /**
     * 配置值
     */
    private String paramType;
}
