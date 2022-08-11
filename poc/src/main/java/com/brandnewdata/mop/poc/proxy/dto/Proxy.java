package com.brandnewdata.mop.poc.proxy.dto;

import lombok.Data;

@Data
public class Proxy {


    /**
     * id
     */
    private Long id;

    /**
     * API 名称
     */
    private String name;

    /**
     * 协议：1 HTTP，2 HTTPS，3 HTTP&HTTPS
     */
    private Integer protocol;

    /**
     * 版本
     */
    private String version;

    /**
     * 描述
     */
    private String description;

    /**
     * 更新时间
     */
    private String updateTime;

    /**
     * 服务域名
     */
    private String domain = "www.brandnewdata.com";
}
