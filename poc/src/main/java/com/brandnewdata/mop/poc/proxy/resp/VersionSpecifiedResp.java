package com.brandnewdata.mop.poc.proxy.resp;

import lombok.Data;

@Data
public class VersionSpecifiedResp {

    /**
     * 版本号
     */
    private String version;
    /**
     * 状态
     */
    private String states;

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
    private int endpointTotal;
}
