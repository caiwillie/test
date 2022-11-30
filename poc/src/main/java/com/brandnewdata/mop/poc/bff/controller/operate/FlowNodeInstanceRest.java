package com.brandnewdata.mop.poc.bff.controller.operate;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.operate.dto.FlowNodeInstanceDto;
import com.brandnewdata.mop.poc.operate.dto.FlowNodeInstanceTreeNodeDto;
import com.brandnewdata.mop.poc.operate.service.FlowNodeInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 流程监控相关的接口
 */
@RestController
public class FlowNodeInstanceRest {

    @Autowired
    private FlowNodeInstanceService service;

    /**
     * 获取流程节点列表
     *
     * @param processInstanceId 流程实例id
     * @return the result
     */
    @GetMapping("/rest/operate/process/flowNodeInstance/list")
    public Result<List<FlowNodeInstanceTreeNodeDto>> list(@RequestParam String processInstanceId) {
        List<FlowNodeInstanceTreeNodeDto> list = service.list(processInstanceId);
        return Result.OK(list);
    }

    /**
     * 获取节点实例的详情（从节点树点击）
     *
     * @param processInstanceId  流程实例id
     * @param flowNodeInstanceId 节点实例id
     * @return the result
     */
    @GetMapping("/rest/operate/flowNodeInstance/detailByFlowNodeInstanceId")
    public Result<FlowNodeInstanceDto> detailByFlowNodeInstanceId(
            @RequestParam String processInstanceId, @RequestParam String flowNodeInstanceId) {
        FlowNodeInstanceDto data = service.detailByFlowNodeInstanceId(processInstanceId, flowNodeInstanceId);
        return Result.OK(data);
    }

    /**
     * 获取节点实例的详情（从图上点击）
     *
     * @param processInstanceId 流程实例id
     * @param flowNodeId        节点id
     * @return the result
     */
    @GetMapping("/rest/operate/flowNodeInstance/detailByFlowNodeId")
    public Result<FlowNodeInstanceDto> detailByFlowNodeId(
            @RequestParam String processInstanceId, @RequestParam String flowNodeId) {
        FlowNodeInstanceDto data = service.detailByFlowNodeId(Long.valueOf(processInstanceId), flowNodeId);
        return Result.OK(data);
    }

}
