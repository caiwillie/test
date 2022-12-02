package com.brandnewdata.mop.poc.proxy.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProxyDto {

    private Long id;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String name;

    private Integer protocol;

    private String version;

    private String description;

    private String domain;

    private String tag;

    private Integer state;
}
