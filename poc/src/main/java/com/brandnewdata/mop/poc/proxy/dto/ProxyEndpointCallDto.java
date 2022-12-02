package com.brandnewdata.mop.poc.proxy.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProxyEndpointCallDto {

    private Long id;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long proxyId;

    private String proxyName;

    private String version;

    private String location;

    private Long endpointId;

    private String ip;

    private String mac;

    private String userAgent;

    private String httpMethod;

    private String httpStatus;

    private String httpQuery;

    private String httpBody;

    private Integer timeConsuming;

}
