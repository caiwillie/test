package com.brandnewdata.mop.poc.process.service;

import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionDTO;
import com.brandnewdata.mop.poc.process.dto.ProcessDeployDTO;

import java.util.Map;

public interface IProcessDeployService {

    /**
     * 部署
     *
     * @param processDefinitionDTO the process definition
     * @param type              1 场景，2 操作，3 触发器
     * @return process definition
     */
    ProcessDeployDTO deploy(ProcessDefinitionDTO processDefinitionDTO, int type);

    Page<ProcessDeployDTO> page(int pageNum, int pageSize);

    Map<String, Object> startWithResult(String processId, Map<String, Object> values);

    Map<String, Object> startWithResultTest(String processId, Map<String, Object> values);

    ProcessDeployDTO getOne(long deployId);
}
