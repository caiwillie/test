package com.brandnewdata.mop.poc.process.service;

import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionDto;
import com.brandnewdata.mop.poc.process.dto.ProcessDeployDto;

import java.util.List;
import java.util.Map;

public interface IProcessDeployService {

    /**
     * 部署流程
     *
     * @param processDefinitionDTO the process definition
     * @param type              1 场景，2 操作，3 触发器
     * @return process definition
     */
    ProcessDeployDto deploy(ProcessDefinitionDto processDefinitionDTO, int type);

    List<ProcessDeployDto> listByType(int type);

    List<ProcessDeployDto> listByIdList(List<Long> idList);

    Page<ProcessDeployDto> page(int pageNum, int pageSize);

    Map<String, Object> startWithResult(String processId, Map<String, Object> values);

    Map<String, Object> startWithResultTest(String processId, Map<String, Object> values);

    ProcessDeployDto getOne(long deployId);
}
