package com.brandnewdata.mop.poc.process.service;

import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionDto;

public interface IProcessDefinitionService {


    /**
     * 保存流程定义
     *
     * @param processDefinitionDTO the process definition
     * @return the process definition
     */
    ProcessDefinitionDto save(ProcessDefinitionDto processDefinitionDTO);







}
