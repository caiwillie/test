/*
package com.brandnewdata.mop.poc.bff.controller.operate;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.operate.dto.FlowNodeStateDto;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDto;
import com.brandnewdata.mop.poc.operate.entity.SequenceFlowEntity;
import com.brandnewdata.mop.poc.operate.resp.FlowNodeStateResp;
import com.brandnewdata.mop.poc.operate.resp.ProcessInstanceResp;
import com.brandnewdata.mop.poc.operate.resp.SequenceFlowResp;
import com.brandnewdata.mop.poc.operate.service.ProcessInstanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

*/
/**
 * 流程监控相关的接口
 *
 *//*

@RestController
public class ProcessInstanceRest {

    @Resource
    private ProcessInstanceService processInstanceService;

    */
/**
     * 获取流程实例的详情
     *
     * @param processInstanceId 流程实例id
     * @return the result
     *//*

    @GetMapping("/rest/operate/process/instance/detail")
    public Result<ProcessInstanceResp> detail(@RequestParam String processInstanceId) {
        ListViewProcessInstanceDto dto = processInstanceService.detail(Long.valueOf(processInstanceId));
        ProcessInstanceResp resp = new ProcessInstanceResp();
        resp.from(dto);
        return Result.OK(resp);
    }

    */
/**
     * 获取流程实例的轨迹连线
     *
     * @param processInstanceId 流程实例id
     * @return the result
     *//*

    @GetMapping("/rest/operate/process/instance/sequenceFlows")
    public Result<List<SequenceFlowResp>> sequenceFlows(@RequestParam String processInstanceId) {
        List<SequenceFlowEntity> sequenceFlowEntities = processInstanceService.sequenceFlows(Long.valueOf(processInstanceId));
        List<SequenceFlowResp> records = sequenceFlowEntities.stream().map(e -> {
            SequenceFlowResp resp = new SequenceFlowResp();
            return resp.from(e);
        }).sorted(Comparator.comparing(SequenceFlowResp::getSequenceFlowId)).collect(Collectors.toList());
        return Result.OK(records);
    }

    */
/**
     * 获取流程实例的节点状态
     *
     * @param processInstanceId 流程实例id
     * @return
     *//*

    @GetMapping("/rest/operate/process/instance/flowNodeStates")
    public Result<List<FlowNodeStateResp>> flowNodeStates(@RequestParam String processInstanceId) {
        List<FlowNodeStateResp> ret = new ArrayList<>();
        Map<String, FlowNodeStateDto> flowNodeStateMap = processInstanceService.getFlowNodeStateMap(Long.valueOf(processInstanceId));
        for (Map.Entry<String, FlowNodeStateDto> entry : flowNodeStateMap.entrySet()) {
            String flowNodeId = entry.getKey();
            FlowNodeStateDto state = entry.getValue();
            FlowNodeStateResp resp = new FlowNodeStateResp();
            resp.setProcessInstanceId(processInstanceId);
            resp.setFlowNodeId(flowNodeId);
            resp.setState(state.name());
            ret.add(resp);
        }
        return Result.OK(ret);
    }
}
*/
