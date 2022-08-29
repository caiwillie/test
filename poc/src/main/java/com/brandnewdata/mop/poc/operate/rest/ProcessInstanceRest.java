package com.brandnewdata.mop.poc.operate.rest;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.operate.dto.FlowNodeStateDTO;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDTO;
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

/**
 * 运行监控相关的接口
 *
 */
@RestController
public class ProcessInstanceRest {

    @Resource
    private ProcessInstanceService processInstanceService;

    /**
     * 获取流程实例分页列表
     *
     * @param deployId 部署id
     * @param pageNum  分页页码
     * @param pageSize 分页大小
     * @return result result
     */
    @GetMapping("/rest/operate/instance/page")
    public Result<Page<ProcessInstanceResp>> page (
            @RequestParam Long deployId,
            @RequestParam int pageNum,
            @RequestParam int pageSize) {
        Page<ListViewProcessInstanceDTO> page = processInstanceService.page(deployId, pageNum, pageSize);


        List<ListViewProcessInstanceDTO> records = page.getRecords();
        List<ProcessInstanceResp> respList = records.stream().map(dto -> {
            ProcessInstanceResp resp = new ProcessInstanceResp();
            return resp.from(dto);
        }).collect(Collectors.toList());

        return Result.OK(new Page<>(page.getTotal(), respList));
    }

    /**
     * 获取流程实例的轨迹连线
     *
     * @param processInstanceId 流程实例id
     * @return the result
     */
    @GetMapping("/rest/operate/instance/sequenceFlows")
    public Result<List<SequenceFlowResp>> sequenceFlows(@RequestParam String processInstanceId) {
        List<SequenceFlowEntity> sequenceFlowEntities = processInstanceService.sequenceFlows(Long.valueOf(processInstanceId));
        List<SequenceFlowResp> records = sequenceFlowEntities.stream().map(e -> {
            SequenceFlowResp resp = new SequenceFlowResp();
            return resp.from(e);
        }).sorted(Comparator.comparing(SequenceFlowResp::getSequenceFlowId)).collect(Collectors.toList());
        return Result.OK(records);
    }

    /**
     * 获取流程实例的节点状态
     *
     * @param processInstanceId 流程实例id
     * @return
     */
    @GetMapping("/rest/operate/instance/flowNodeStates")
    public Result<List<FlowNodeStateResp>> flowNodeStates(@RequestParam String processInstanceId) {
        List<FlowNodeStateResp> ret = new ArrayList<>();
        Map<String, FlowNodeStateDTO> flowNodeStateMap = processInstanceService.getFlowNodeStateMap(Long.valueOf(processInstanceId));
        for (Map.Entry<String, FlowNodeStateDTO> entry : flowNodeStateMap.entrySet()) {
            String flowNodeId = entry.getKey();
            FlowNodeStateDTO state = entry.getValue();
            FlowNodeStateResp resp = new FlowNodeStateResp();
            resp.setProcessInstanceId(processInstanceId);
            resp.setFlowNodeId(flowNodeId);
            resp.setState(state.name());
            ret.add(resp);
        }
        return Result.OK(ret);
    }
}
