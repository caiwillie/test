package com.brandnewdata.mop.poc.bff.vo.scene.external;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PrepareLoadVo {

    /**
     * id
     */
    private String id;

    /**
     * 项目id
     */
    private String projectId;

    /**
     * 新场景名称
     */
    private String sceneName;

    /**
     * 配置列表
     */
    private List<ConnectorConfigVo> configureList;
}
