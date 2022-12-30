package com.brandnewdata.mop.poc.bff.vo.scene.external;

import lombok.Getter;
import lombok.Setter;

/**
 * 连接器配置
 */
@Getter
@Setter
public class ConnectorConfigVo {

    /**
     * 连接器所属组
     */
    private String connectorGroup;

    /**
     * 连接器id
     */
    private String connectorId;

    /**
     * 连接器名称
     */
    private String connectorName;

    /**
     * 连接器版本
     */
    private String connectorVersion;

    /**
     * 连接器类型
     */
    private Integer connectorType;

    /**
     * 连接器icon
     */
    private String connectorIcon;

    /**
     * 配置名称
     */
    private String configureName;

    /**
     * 配置id
     */
    private String configureId;

    /**
     * 替换配置名称
     */
    private String newConfigureId;
}
