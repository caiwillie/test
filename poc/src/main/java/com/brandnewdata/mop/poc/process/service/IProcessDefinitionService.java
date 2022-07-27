package com.brandnewdata.mop.poc.process.service;

import com.brandnewdata.mop.poc.process.dto.ProcessDefinition;

import java.util.List;

public interface IProcessDefinitionService {

    /**
     * 根据 id list 获取定义列表
     * @param ids
     * @return
     */
    List<ProcessDefinition> list(List<String> ids);

    /**
     * 保存流程定义
     *
     * @param processDefinition the process definition
     * @return the process definition
     */
    ProcessDefinition save(ProcessDefinition processDefinition);

    ProcessDefinition getOne(String processId);

    /**
     * 部署
     *
     * @param processDefinition
     * @param type 1 场景，2 操作，3 触发器
     * @return
     */
    ProcessDefinition deploy(ProcessDefinition processDefinition, int type);
}
