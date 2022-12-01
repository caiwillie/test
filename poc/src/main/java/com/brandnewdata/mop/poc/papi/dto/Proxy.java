package com.brandnewdata.mop.poc.papi.dto;

import lombok.Data;

import java.time.LocalDateTime;

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
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 服务域名
     */
    private String domain;

    /**
     * 标签
     */
    private String tag;

    /**
     * 状态：1 停止，2 运行，3 开发中
     */
    private Integer state;
}
