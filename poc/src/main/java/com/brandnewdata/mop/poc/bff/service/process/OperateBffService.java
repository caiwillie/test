package com.brandnewdata.mop.poc.bff.service.process;

import com.brandnewdata.mop.poc.bff.converter.operate.SequenceFlowVoConverter;
import com.brandnewdata.mop.poc.bff.vo.operate.process.FlowNodeStateVo;
import com.brandnewdata.mop.poc.bff.vo.operate.process.SequenceFlowVo;
import com.brandnewdata.mop.poc.bff.vo.operate.process.VariableVo;
import com.brandnewdata.mop.poc.operate.dto.FlowNodeStateDto;
import com.brandnewdata.mop.poc.operate.dto.SequenceFlowDto;
import com.brandnewdata.mop.poc.operate.service.IProcessInstanceService2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OperateBffService {

    private final IProcessInstanceService2 processInstanceService;

    public OperateBffService(IProcessInstanceService2 processInstanceService) {
        this.processInstanceService = processInstanceService;
    }

    public void detailProcessInstance() {

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
        return null;
    }

    public List listFlowNodeInstance(Long envId, String processInstanceId) {
        return null;
    }

    public void detailFlowNodeInstanceById(Long envId, String flowNodeInstanceId) {
        return;
    }

    public void detailFlowNodeInstanceByFlowNodeId(Long envId, String processInstanceId, String flowNodeId) {
        return;
    }

}
