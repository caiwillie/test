package com.brandnewdata.mop.poc.proxy.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProxyEndpointDto {

    private Long id;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long proxyId;

    private String location;

    private String description;

    private Integer backendType;

    private String backendConfig;

    private String tag;
}
