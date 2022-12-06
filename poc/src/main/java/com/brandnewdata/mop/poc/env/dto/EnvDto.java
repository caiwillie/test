package com.brandnewdata.mop.poc.env.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class EnvDto {
    private Long id;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime deployTime;

    private String name;

    private String namespace;

    private String status;

    private Integer type;

    private String description;

    private String httpListenerDomainIdentifier;
}
