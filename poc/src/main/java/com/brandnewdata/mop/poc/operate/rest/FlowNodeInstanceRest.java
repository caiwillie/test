package com.brandnewdata.mop.poc.operate.rest;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.operate.dto.FlowNodeInstanceDetailDTO;
import com.brandnewdata.mop.poc.operate.dto.FlowNodeInstanceListDTO;
import com.brandnewdata.mop.poc.operate.service.FlowNodeInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 运行监控相关的接口
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
    @GetMapping("/rest/operate/flowNodeInstance/list")
    public Result<List<FlowNodeInstanceListDTO>> list(@RequestParam String processInstanceId) {
        List<FlowNodeInstanceListDTO> list = service.list(processInstanceId);
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
    public Result<FlowNodeInstanceDetailDTO> detailByFlowNodeInstanceId(
            @RequestParam String processInstanceId, @RequestParam String flowNodeInstanceId) {
        FlowNodeInstanceDetailDTO data = service.detailByFlowNodeInstanceId(processInstanceId, flowNodeInstanceId);
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
    public Result<FlowNodeInstanceDetailDTO> detailByFlowNodeId(
            @RequestParam String processInstanceId, @RequestParam String flowNodeId) {
        return null;
    }

}
