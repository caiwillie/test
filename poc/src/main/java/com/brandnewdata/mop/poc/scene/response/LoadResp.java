package com.brandnewdata.mop.poc.scene.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LoadResp {

    /**
     * 目录路径
     */
    private Long id;

    /**
     * 配置列表
     */
    private List<ConnConfResp> configureList;
}
