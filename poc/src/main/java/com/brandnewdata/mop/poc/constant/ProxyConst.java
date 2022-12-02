package com.brandnewdata.mop.poc.constant;

public interface ProxyConst {

    /**
     * 已停止
     */
    int STATE_STOP = 1;

    /**
     * 运行中
     */
    int STATE_RUNNING = 2;

    /**
     * 配置中
     */
    int STATE_DEVELOPING = 3;

    String FORMAT_JSON = "JSON";

    String FORMAT_YAML = "YAML";

    Integer BACKEND_TYPE__SCENE = 1;

    Integer BACKEND_TYPE__SERVER = 2;
}
