package com.brandnewdata.mop.poc.papi.req;

import lombok.Data;

@Data
public class ProxyReq {
    /**
     * id（新增时不传；更新、删除时传）
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
     * 1 停止（操作显示运行），
     * 2 运行（操作显示停止）
     */
    private Integer state;

    /**
     * 描述
     */
    private String description;

}
