package com.brandnewdata.mop.poc.env.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

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
}
