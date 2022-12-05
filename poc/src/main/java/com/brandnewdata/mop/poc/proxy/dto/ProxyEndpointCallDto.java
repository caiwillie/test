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

    private LocalDateTime startTime;

    private Long proxyId;

    private String proxyName;

    private String version;

    private String location;

    private Long endpointId;

    private String ipAddress;

    private String macAddress;

    private String userAgent;

    private String httpMethod;

    private String httpStatus;

    private String requestQuery;

    private String requestBody;

    private Integer timeConsuming;

    private String responseBody;

    private String errorMessage;

}
