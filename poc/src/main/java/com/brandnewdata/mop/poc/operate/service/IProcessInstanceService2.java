package com.brandnewdata.mop.poc.operate.service;

import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.operate.dto.FlowNodeStateDto;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDto;
import com.brandnewdata.mop.poc.operate.dto.SequenceFlowDto;

import java.util.List;
import java.util.Map;

public interface IProcessInstanceService2 {

    Page<ListViewProcessInstanceDto> pageProcessInstanceByZeebeKey(
            Long envId,
            List<Long> zeebeKeyList,
            int pageNum,
            int pageSize,
            Map<String, Object> extraMap);

    List<ListViewProcessInstanceDto> listProcessInstanceByZeebeKey(
            Long envId,
            List<Long> zeebeKeyList);

    ListViewProcessInstanceDto detailProcessInstance(Long envId, Long processInstanceId);

    List<SequenceFlowDto> sequenceFlows(Long envId, Long processInstanceId);

    Map<String, FlowNodeStateDto> getFlowNodeStateMap(Long envId, Long processInstanceId);
}
