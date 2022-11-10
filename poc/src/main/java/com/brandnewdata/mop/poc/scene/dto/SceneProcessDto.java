package com.brandnewdata.mop.poc.scene.dto;

import lombok.Data;

/**
 * @author caiwillie
 */
@Data
public class SceneProcessDto {
    /**
     * id
     */
    private Long id;

    /**
     * 场景 id
     */
    private Long businessSceneId;

    /**
     * 流程 id
     */
    private String processId;

    /**
     * 流程名称
     */
    private String name;

    /**
     * 流程 xml
     */
    private String xml;

    /**
     * 略缩图的 url
     */
    private String imgUrl;
}
