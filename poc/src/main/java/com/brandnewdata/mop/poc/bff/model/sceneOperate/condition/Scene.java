package com.brandnewdata.mop.poc.bff.model.sceneOperate.condition;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Scene {
    /**
     * 场景 id
     */
    private Long id;

    /**
     * 场景名称
     */
    private String name;

    /**
     * 流程列表
     */
    private List<Process> processList;

}
