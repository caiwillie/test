package com.brandnewdata.mop.poc.scene.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SceneDto {
    /**
     * 场景 id
     */
    private Long id;

    /**
     * 场景名称
     */
    private String name;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;



}
