package com.brandnewdata.mop.poc.modeler.service;

import com.brandnewdata.mop.poc.modeler.dto.ProcessDefinition;

import java.util.List;

public interface IProcessDefinitionService {

    /**
     * 根据 id list 获取定义列表
     * @param ids
     * @return
     */
    List<ProcessDefinition> list(List<String> ids);


    ProcessDefinition save(String processId, String name, String xml);
}
