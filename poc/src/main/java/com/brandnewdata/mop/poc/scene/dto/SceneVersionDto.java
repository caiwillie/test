package com.brandnewdata.mop.poc.scene.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SceneVersionDto {
    private Long id;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String version;

    private Long sceneId;

    private Integer status;

    private Double deployProgressPercentage;

    private String exceptionMessage;

    private Integer deployStatus;

}
