package com.brandnewdata.mop.poc.scene.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SceneReleaseDeployDto {
    private Long id;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long sceneId;

    private String sceneName;

    private Long versionId;

    private String versionName;

    private String processId;

    private String processName;

    private Long envId;
}
