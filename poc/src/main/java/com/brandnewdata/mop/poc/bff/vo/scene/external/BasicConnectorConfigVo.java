package com.brandnewdata.mop.poc.bff.vo.scene.external;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BasicConnectorConfigVo {
    /**
     * 连接器配置id
     */
    private String configId;

    /**
     * 连接器配置名称
     */
    private String configName;
}
