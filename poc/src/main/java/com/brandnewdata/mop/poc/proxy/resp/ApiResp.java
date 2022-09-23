package com.brandnewdata.mop.poc.proxy.resp;

import lombok.Data;

import java.util.List;

@Data
public class ApiResp {

    /**
     * API 名称
     */
    private String name;

    /**
     * 版本列表
     */
    private List<String> versions;

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
