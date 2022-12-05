package com.brandnewdata.mop.poc.constant;

public interface ProxyConst {

    /**
     * 已停止
     */
    int PROXY_STATE__STOPPED = 1;

    /**
     * 运行中
     */
    int PROXY_STATE__RUNNING = 2;

    /**
     * 配置中
     */
    int PROXY_STATE__DEVELOPING = 3;

    int PROXY_PROTOCOL__HTTP = 1;

    int PROXY_PROTOCOL__HTTPS = 2;

    int PROXY_PROTOCOL__HTTP_AND_HTTPS = 3;

    String FORMAT_JSON = "JSON";

    String FORMAT_YAML = "YAML";

    Integer BACKEND_TYPE__SCENE = 1;

    Integer BACKEND_TYPE__SERVER = 2;
}
