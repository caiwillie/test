package com.brandnewdata.mop.poc.bff.model.sceneOperate.condition;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Process {

    /**
     * 流程id
     */
    private String processId;

    /**
     * 流程名称
     */
    private String name;

    /**
     * 版本列表
     */
    private List<Integer> versionList;

}
