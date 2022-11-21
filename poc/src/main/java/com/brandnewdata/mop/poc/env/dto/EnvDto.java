package com.brandnewdata.mop.poc.env.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class EnvDto {
    private Long id;
    
    private Date createTime;

    private Date updateTime;

    private Date deployTime;

    private String name;

    private Integer status;

    private Integer type;

    private String description;
}
