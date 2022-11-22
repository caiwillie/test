package com.brandnewdata.mop.poc.env.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

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
