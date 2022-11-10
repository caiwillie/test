package com.brandnewdata.mop.poc.scene.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LoadResp {

    /**
     * id
     */
    private Long id;

    /**
     * 场景名称
     */
    private String sceneName;

    /**
     * 配置列表
     */
    private List<ConnConfResp> configureList;
}
