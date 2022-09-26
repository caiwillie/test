package com.brandnewdata.mop.poc.proxy.resp;

import lombok.Data;

@Data
public class VersionSpecifiedResp {
    /**
     * API 的ID
     */
    private Long id;

    /**
     * 版本号
     */
    private String version;
    /**
     * 1 停止（操作显示运行），
     * 2 运行（操作显示停止）
     */
    private Integer state;

    /**
     * 服务域名
     */
    private String domain;

    /**
     * 更新时间
     */
    private String updateTime;

    /**
     * endpoint总数
     */
    private Long endpointTotal;
}
