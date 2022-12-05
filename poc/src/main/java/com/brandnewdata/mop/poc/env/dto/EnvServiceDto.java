package com.brandnewdata.mop.poc.env.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EnvServiceDto {
    private Long id;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String name;

    private Long envId;

    private String clusterIp;

    private String ports;

}
