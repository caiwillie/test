package com.brandnewdata.mop.poc.operate.service;

import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.operate.dto.FlowNodeStateDto;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDto;
import com.brandnewdata.mop.poc.operate.dto.SequenceFlowDto;
import com.brandnewdata.mop.poc.operate.dto.filter.ProcessInstanceFilter;

import java.util.List;
import java.util.Map;

public interface IProcessInstanceService {

    Page<ListViewProcessInstanceDto> pageProcessInstanceByZeebeKey(
            Long envId,
            List<Long> zeebeKeyList,
            int pageNum,
            int pageSize,
            ProcessInstanceFilter filter,
            Map<String, Object> extraMap);

    List<ListViewProcessInstanceDto> listProcessInstanceByZeebeKey(
            Long envId,
            List<Long> zeebeKeyList,
            ProcessInstanceFilter filter);

    List<ListViewProcessInstanceDto> listProcessInstanceCacheByZeebeKey(
            Long envId,
            List<Long> zeebeKeyList,
            ProcessInstanceFilter filter);

    ListViewProcessInstanceDto detailProcessInstance(Long envId, Long processInstanceId);

    List<SequenceFlowDto> sequenceFlows(Long envId, Long processInstanceId);

    Map<String, FlowNodeStateDto> getFlowNodeStateMap(Long envId, Long processInstanceId);
}
