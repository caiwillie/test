package com.brandnewdata.mop.poc.proxy.req;

import lombok.Data;

@Data
public class EndpointReq {
    /**
     * id
     * 新增时不传；更新、删除时传
     */
    private Long id;

    /**
     * api 的 id
     */
    private Long proxyId;

    /**
     * 位置
     */
    private String location;

    /**
     * 后端服务类型：1 集成流，2 第三方服务 base url
     */
    private Integer backendType;

    /**
     * 后端服务配置（通过json.stringify序列化成字符串）
     */
    private String backendConfig;

    /**
     * 描述
     */
    private String description;
}
