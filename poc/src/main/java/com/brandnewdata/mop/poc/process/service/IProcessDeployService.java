package com.brandnewdata.mop.poc.process.service;

import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinition;
import com.brandnewdata.mop.poc.process.dto.ProcessDeploy;

import java.util.Map;

public interface IProcessDeployService {

    /**
     * 部署
     *
     * @param processDefinition the process definition
     * @param type              1 场景，2 操作，3 触发器
     * @return process definition
     */
    ProcessDeploy deploy(ProcessDefinition processDefinition, int type);

    Page<ProcessDeploy> page(int pageNum, int pageSize);

    Map<String, Object> startWithResult(String processId, Map<String, Object> value);

    ProcessDeploy getOne(long deployId);
}
