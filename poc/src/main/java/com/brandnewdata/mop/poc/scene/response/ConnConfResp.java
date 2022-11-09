package com.brandnewdata.mop.poc.scene.response;

import lombok.Getter;
import lombok.Setter;

/**
 * 连接器配置
 */
@Getter
@Setter
public class ConnConfResp {

    /**
     * 连接器名称
     */
    private String connectorName;

    /**
     * 连接器id
     */
    private String connectorType;

    /**
     * 连接器版本
     */
    private String connectorVersion;

    /**
     * 配置名称
     */
    private String configureId;

    /**
     * 替换配置名称
     */
    private String newConfigureId;
}
