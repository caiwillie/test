package com.brandnewdata.mop.poc.process.service;

import com.brandnewdata.mop.poc.process.dto.ProcessDefinition;

import java.util.List;

public interface IProcessDefinitionService {

    /**
     * 根据 id list 获取定义列表
     *
     * @param ids the ids
     * @return list
     */
    List<ProcessDefinition> list(List<String> ids);

    /**
     * 保存流程定义
     *
     * @param processDefinition the process definition
     * @return the process definition
     */
    ProcessDefinition save(ProcessDefinition processDefinition);

    /**
     * 获取流程定义详情
     *
     * @param processId the process id
     * @return the one
     */
    ProcessDefinition getOne(String processId);




}
