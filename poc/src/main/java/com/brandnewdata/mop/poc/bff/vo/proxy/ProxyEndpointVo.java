package com.brandnewdata.mop.poc.bff.vo.proxy;

import lombok.Data;

@Data
public class ProxyEndpointVo {

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
    private Integer backendType;

    /**
     * 后端服务配置（通过json.stringify序列化成字符串）
     *
     * 如果是集成流，json格式如下
     * {
     *     Long envId;
     *     String envName;
     *     Long sceneId;
     *     String sceneName;
     *     Long versionId;
     *     String versionName;
     *     String processId;
     *     String processName;
     * }
     *
     * 如果是第三方服务，内容为输入的baseUrl
     */
    private String backendConfig;

    /**
     * 描述
     */
    private String description;

    /**
     * 分组
     */
    private String tag;
}
