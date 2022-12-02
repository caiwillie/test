package com.brandnewdata.mop.poc.bff.vo.proxy.operate;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EndpointCallVo {

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
     * 来源
     */
    private String source;

    /**
     * proxy id
     */
    private Long proxyId;

    /**
     * api 名称
     */
    private String proxyName;

    /**
     * endpoint id
     */
    private Long endpointId;

    /**
     * 路径
     */
    private String location;

    /**
     * 请求方法
     */
    private String httpMethod;

    /**
     * 返回状态
     */
    private String httpStatus;

    /**
     * 请求体
     */
    private String httpBody;

    /**
     * 请求耗时
     */
    private double timeConsuming;


}
