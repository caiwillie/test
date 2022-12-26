package com.brandnewdata.mop.poc.scene.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SceneProcessDto {

    /**
     * id
     */
    private Long id;

    /**
     * 场景id
     */
    private Long sceneId;

    /**
     * 流程id
     */
    private String processId;
}
