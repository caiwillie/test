package com.brandnewdata.mop.poc.bff.service.operate;

import com.brandnewdata.mop.poc.bff.converter.operate.ProcessInstanceVoConverter;
import com.brandnewdata.mop.poc.bff.converter.operate.SequenceFlowVoConverter;
import com.brandnewdata.mop.poc.bff.converter.operate.VariableVoConverter;
import com.brandnewdata.mop.poc.bff.vo.operate.process.FlowNodeStateVo;
import com.brandnewdata.mop.poc.bff.vo.operate.process.ProcessInstanceVo;
import com.brandnewdata.mop.poc.bff.vo.operate.process.SequenceFlowVo;
import com.brandnewdata.mop.poc.bff.vo.operate.process.VariableVo;
import com.brandnewdata.mop.poc.operate.dto.*;
import com.brandnewdata.mop.poc.operate.service.IFlowNodeInstanceService2;
import com.brandnewdata.mop.poc.operate.service.IProcessInstanceService2;
import com.brandnewdata.mop.poc.operate.service.IVariableService2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OperateBffService {

    private final IProcessInstanceService2 processInstanceService;

    private final IVariableService2 variableService;

    private final IFlowNodeInstanceService2 flowNodeInstanceService;

    public OperateBffService(IProcessInstanceService2 processInstanceService,
                             IVariableService2 variableService,
                             IFlowNodeInstanceService2 flowNodeInstanceService) {
        this.processInstanceService = processInstanceService;
        this.variableService = variableService;
        this.flowNodeInstanceService = flowNodeInstanceService;
    }

    public ProcessInstanceVo detailProcessInstance(Long envId, String processInstanceId) {
        ListViewProcessInstanceDto processInstanceDto = processInstanceService.detailProcessInstance(envId, Long.valueOf(processInstanceId));
        return ProcessInstanceVoConverter.createFrom(processInstanceDto);
    }

    public List<SequenceFlowVo> listSequenceFlows(Long envId, String processInstanceId) {
        List<SequenceFlowDto> sequenceFlowDtoList = processInstanceService.sequenceFlows(envId, Long.valueOf(processInstanceId));
        return sequenceFlowDtoList.stream().sorted(Comparator.comparing(SequenceFlowDto::getActivityId))
                .map(SequenceFlowVoConverter::createFrom)
                .collect(Collectors.toList());
    }

    public List<FlowNodeStateVo> listFlowNodeStates(Long envId, String processInstanceId) {
        List<FlowNodeStateVo> ret = new ArrayList<>();
        Map<String, FlowNodeStateDto> flowNodeStateMap = processInstanceService.getFlowNodeStateMap(envId, Long.valueOf(processInstanceId));
        for (Map.Entry<String, FlowNodeStateDto> entry : flowNodeStateMap.entrySet()) {
            String flowNodeId = entry.getKey();
            FlowNodeStateDto state = entry.getValue();
            FlowNodeStateVo vo = new FlowNodeStateVo();
            vo.setProcessInstanceId(processInstanceId);
            vo.setFlowNodeId(flowNodeId);
            vo.setState(state.name());
            ret.add(vo);
        }
        return ret;
    }

    public List<VariableVo> listVariable(Long envId,
                                         String processInstanceId,
                                         String scopeId) {
        List<VariableDto> variableDtoList = variableService.listByScopeId(envId, processInstanceId, scopeId);
        return variableDtoList.stream().map(VariableVoConverter::createFrom).collect(Collectors.toList());
    }

    public List<FlowNodeInstanceTreeNodeDto> listFlowNodeInstance(Long envId, String processInstanceId) {
        return flowNodeInstanceService.list(envId, processInstanceId);
    }

    public FlowNodeInstanceDto detailFlowNodeInstanceById(Long envId, String flowNodeInstanceId) {
        return flowNodeInstanceService.detailById(envId, flowNodeInstanceId);
    }

    public FlowNodeInstanceDto detailFlowNodeInstanceByFlowNodeId(Long envId, String processInstanceId, String flowNodeId) {
        return flowNodeInstanceService.detailByFlowNodeId(envId, Long.valueOf(processInstanceId), flowNodeId);
    }

}
