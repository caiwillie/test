package com.brandnewdata.mop.poc.process.service;

import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionDto;

import java.util.List;

public interface IProcessDefinitionService {

    /**
     * 根据 id list 获取定义列表
     *
     * @param ids the ids
     * @return list
     */
    List<ProcessDefinitionDto> list(List<String> ids, boolean withXML);

    /**
     * 保存流程定义
     *
     * @param processDefinitionDTO the process definition
     * @return the process definition
     */
    ProcessDefinitionDto save(ProcessDefinitionDto processDefinitionDTO);

    void delete(ProcessDefinitionDto processDefinitionDTO);

    /**
     * 获取流程定义详情
     *
     * @param processId the process id
     * @return the one
     */
    ProcessDefinitionDto getOne(String processId);




}
