package com.brandnewdata.mop.poc.bff.vo.proxy;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProxyVo {
    /**
     * 项目id
     */
    private String projectId;

    /**
     * id
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

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
     * 服务域名
     */
    private String domain;

    /**
     * 状态：1 停止，2 运行，3 开发中
     */
    private Integer state;

    /**
     * 标签
     */
    private String tag;

    /**
     * 描述
     */
    private String description;

    /**
     * endpoint总数
     */
    private Long endpointTotal;

    /**
     * 24小时内的调用次数
     */
    private Long callTimes24h;
}
