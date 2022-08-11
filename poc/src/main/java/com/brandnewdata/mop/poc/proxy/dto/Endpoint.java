package com.brandnewdata.mop.poc.proxy.dto;

import lombok.Data;

@Data
public class Endpoint {

    /**
     * id
     */
    private Long id;

    /**
     * api id
     */
    private Long proxyId;

    /**
     * 位置
     */
    private String location;

    /**
     * 后端服务类型：1 集成流，2 第三方服务 base url
     */
    private int backendType;

    /**
     * 后端服务配置（通过json.stringify序列化成字符串）
     */
    private String backendConfig;

    /**
     * 描述
     */
    private String description;
}
