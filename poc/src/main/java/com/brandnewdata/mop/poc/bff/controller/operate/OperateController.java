package com.brandnewdata.mop.poc.bff.controller.operate;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.bff.vo.operate.process.FlowNodeStateVo;
import com.brandnewdata.mop.poc.bff.vo.operate.process.InstanceVo;
import com.brandnewdata.mop.poc.bff.vo.operate.process.SequenceFlowVo;
import com.brandnewdata.mop.poc.bff.vo.operate.process.VariableVo;
import com.brandnewdata.mop.poc.operate.dto.FlowNodeInstanceDetailDto;
import com.brandnewdata.mop.poc.operate.dto.FlowNodeInstanceDto;
import com.brandnewdata.mop.poc.operate.dto.VariableDto;
import com.brandnewdata.mop.poc.operate.resp.FlowNodeStateResp;
import com.brandnewdata.mop.poc.operate.resp.ProcessInstanceResp;
import com.brandnewdata.mop.poc.operate.resp.SequenceFlowResp;
import com.brandnewdata.mop.poc.operate.service.FlowNodeInstanceService;
import com.brandnewdata.mop.poc.operate.service.ProcessInstanceService;
import com.brandnewdata.mop.poc.operate.service.VariableService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 流程监控相关的接口
 */
@RestController
public class OperateController {

    private final ProcessInstanceService processInstanceService;

    private final VariableService variableService;

    private final FlowNodeInstanceService service;

    public OperateController(ProcessInstanceService processInstanceService,
                             VariableService variableService,
                             FlowNodeInstanceService service) {
        this.processInstanceService = processInstanceService;
        this.variableService = variableService;
        this.service = service;
    }

    /**
     * 获取流程实例的详情
     *
     * @param processInstanceId 流程实例id
     * @param envId             环境id
     * @return the result
     */
    @GetMapping("/rest/debug/process/instance/detail")
    public Result<InstanceVo> detail(@RequestParam String processInstanceId, @RequestParam Long envId) {
        return null;
    }

    /**
     * 获取流程实例的轨迹连线
     *
     * @param processInstanceId 流程实例id
     * @param envId             环境id
     * @return the result
     */
    @GetMapping("/rest/debug/process/instance/sequenceFlows")
    public Result<List<SequenceFlowVo>> sequenceFlows(@RequestParam String processInstanceId, @RequestParam Long envId) {
        return null;
    }

    /**
     * 获取流程实例的节点状态
     *
     * @param processInstanceId 流程实例id
     * @param envId             环境id
     * @return result
     */
    @GetMapping("/rest/debug/process/instance/flowNodeStates")
    public Result<List<FlowNodeStateVo>> flowNodeStates(@RequestParam String processInstanceId, @RequestParam Long envId) {
        return null;
    }

    /**
     * 获取流程变量列表（根据scopeId）
     *
     * @param processInstanceId 流程实例id
     * @param scopeId           scope id（通常是flowNodeInstanceId）
     * @param envId             环境id
     * @return the result
     */
    @GetMapping("/rest/operate/process/variable/listByScopeId")
    public Result<List<VariableVo>> listByFlowNodeInstance(
            @RequestParam String processInstanceId,
            @RequestParam String scopeId,
            @RequestParam Long envId) {
        List<VariableDto> list = variableService.listByScopeId(processInstanceId, scopeId);
        return null;
    }

    /**
     * 获取流程节点列表
     *
     * @param processInstanceId 流程实例id
     * @param envId             环境id
     * @return the result
     */
    @GetMapping("/rest/operate/process/flowNodeInstance/list")
    public Result<List<FlowNodeInstanceDto>> list(
            @RequestParam String processInstanceId,
            @RequestParam Long envId) {
        List<FlowNodeInstanceDto> list = service.list(processInstanceId);
        return Result.OK(list);
    }

    /**
     * 获取节点实例的详情（从节点树点击）
     *
     * @param processInstanceId  流程实例id
     * @param flowNodeInstanceId 节点实例id
     * @param envId              环境id
     * @return the result
     */
    @GetMapping("/rest/operate/flowNodeInstance/detailByFlowNodeInstanceId")
    public Result<FlowNodeInstanceDetailDto> detailByFlowNodeInstanceId(
            @RequestParam String processInstanceId,
            @RequestParam String flowNodeInstanceId,
            @RequestParam Long envId) {
        FlowNodeInstanceDetailDto data = service.detailByFlowNodeInstanceId(processInstanceId, flowNodeInstanceId);
        return Result.OK(data);
    }

    /**
     * 获取节点实例的详情（从图上点击）
     *
     * @param processInstanceId 流程实例id
     * @param flowNodeId        节点id
     * @param envId             环境id
     * @return the result
     */
    @GetMapping("/rest/operate/flowNodeInstance/detailByFlowNodeId")
    public Result<FlowNodeInstanceDetailDto> detailByFlowNodeId(
            @RequestParam String processInstanceId,
            @RequestParam String flowNodeId,
            @RequestParam Long envId) {
        FlowNodeInstanceDetailDto data = service.detailByFlowNodeId(Long.valueOf(processInstanceId), flowNodeId);
        return Result.OK(data);
    }
}
