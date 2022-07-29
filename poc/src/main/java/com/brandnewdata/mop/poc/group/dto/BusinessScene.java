package com.brandnewdata.mop.poc.group.dto;

import lombok.Data;

import java.util.List;

@Data
public class BusinessScene {


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
    private String createTime;

    /**
     * 修改时间
     */
    private String updateTime;

    /**
     * 图片URL
     */
    private String imgUrl;

    /**
     * 流程定义列表
     */
    private List<BusinessSceneProcessDefinition> processDefinitions;
}
