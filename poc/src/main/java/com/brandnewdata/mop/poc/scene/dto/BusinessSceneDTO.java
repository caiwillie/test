package com.brandnewdata.mop.poc.scene.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BusinessSceneDTO {


    /**
     * 场景 id
     */
    private Long id;

    /**
     * 项目id
     */
    private String projectId;

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

    /**
     * 图片URL
     */
    private String imgUrl;

    /**
     * 流程定义列表
     */
    private List<BusinessSceneProcessDTO> processDefinitions;
}
