package com.brandnewdata.mop.poc.proxy.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

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
