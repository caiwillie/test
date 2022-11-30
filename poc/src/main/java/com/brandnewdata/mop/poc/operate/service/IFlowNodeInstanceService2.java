package com.brandnewdata.mop.poc.operate.service;

import com.brandnewdata.mop.poc.operate.dto.FlowNodeInstanceDto;
import com.brandnewdata.mop.poc.operate.dto.FlowNodeInstanceTreeNodeDto;

import java.util.List;

public interface IFlowNodeInstanceService2 {

    List<FlowNodeInstanceTreeNodeDto> list(Long envId, String processInstanceId);

    FlowNodeInstanceDto detailById(Long envId, String flowNodeInstanceId);

    FlowNodeInstanceDto detailByFlowNodeId(Long envId, Long processInstanceId, String flowNodeId);
}
