package com.brandnewdata.mop.poc.bff.vo.proxy.operate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProxyEndpointCallFilter {
    /**
     * 项目id
     */
    private String projectId;

    /**
     * pageNum
     */
    private Integer pageNum;

    /**
     * pageSize
     */
    private Integer pageSize;

    /**
     * proxy 名称
     */
    private String proxyName;

    /**
     *
     */
    private String version;

    /**
     * 路径
     */
    private String location;

    /**
     * 开始时间 yyyy-MM-dd HH:mm:ss
     */
    private String startTime;

    /**
     * 结束时间 yyyy-MM-dd HH:mm:ss
     */
    private String endTime;

}
