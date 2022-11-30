package com.brandnewdata.mop.poc.bff.controller.operate;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.bff.service.operate.OperateBffService;
import com.brandnewdata.mop.poc.bff.vo.operate.process.FlowNodeStateVo;
import com.brandnewdata.mop.poc.bff.vo.operate.process.ProcessInstanceVo;
import com.brandnewdata.mop.poc.bff.vo.operate.process.SequenceFlowVo;
import com.brandnewdata.mop.poc.bff.vo.operate.process.VariableVo;
import com.brandnewdata.mop.poc.operate.dto.FlowNodeInstanceDto;
import com.brandnewdata.mop.poc.operate.dto.FlowNodeInstanceTreeNodeDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 流程监控相关的接口
 */
@RestController
public class OperateController {

    private final OperateBffService operateBffService;

    public OperateController(OperateBffService operateBffService) {
        this.operateBffService = operateBffService;
    }

    /**
     * 获取流程实例的详情
     *
     * @param processInstanceId 流程实例id
     * @param envId             环境id
     * @return the result
     */
    // @GetMapping("/rest/operate/process/processInstance/detail")
    public Result<ProcessInstanceVo> detailProcessInstance(@RequestParam String processInstanceId, @RequestParam Long envId) {
        return null;
    }

    /**
     * 获取流程实例的轨迹连线
     *
     * @param envId             环境id
     * @param processInstanceId 流程实例id
     * @return the result
     */
    @GetMapping("/rest/operate/process/sequenceFlows/list")
    public Result<List<SequenceFlowVo>> listSequenceFlows(
            @RequestParam Long envId,
            @RequestParam String processInstanceId) {
        return null;
    }

    /**
     * 获取流程实例的节点状态
     *
     * @param envId             环境id
     * @param processInstanceId 流程实例id
     * @return result
     */
    @GetMapping("/rest/operate/process/flowNodeStates/list")
    public Result<List<FlowNodeStateVo>> listFlowNodeStates(
            @RequestParam Long envId,
            @RequestParam String processInstanceId) {
        return null;
    }

    /**
     * 获取流程变量列表（根据scopeId）
     *
     * @param envId             环境id
     * @param processInstanceId 流程实例id
     * @param scopeId           scope id（通常是flowNodeInstanceId）
     * @return the result
     */
    @GetMapping("/rest/operate/process/variable/list")
    public Result<List<VariableVo>> listVariable(
            @RequestParam Long envId,
            @RequestParam String processInstanceId,
            @RequestParam String scopeId) {

        return null;
    }

    /**
     * 获取流程节点列表
     *
     * @param envId             环境id
     * @param processInstanceId 流程实例id
     * @return the result
     */
    @GetMapping("/rest/operate/process/flowNodeInstance/list")
    public Result<List<FlowNodeInstanceTreeNodeDto>> listFlowNodeInstance(
            @RequestParam Long envId,
            @RequestParam String processInstanceId) {

        return Result.OK();
    }

    /**
     * 获取节点实例的详情（从节点树点击）
     *
     * @param envId              环境id
     * @param flowNodeInstanceId 节点实例id
     * @return the result
     */
    @GetMapping("/rest/operate/process/flowNodeInstance/detailById")
    public Result<FlowNodeInstanceDto> detailFlowNodeInstanceById(
            @RequestParam Long envId,
            @RequestParam String flowNodeInstanceId) {

        return Result.OK();
    }

    /**
     * 获取节点实例的详情（从图上点击）
     *
     * @param envId             环境id
     * @param processInstanceId 流程实例id
     * @param flowNodeId        节点id
     * @return the result
     */
    @GetMapping("/rest/operate/process/flowNodeInstance/detailByFlowNodeId")
    public Result<FlowNodeInstanceDto> detailFlowNodeInstanceByFlowNodeId(
            @RequestParam Long envId,
            @RequestParam String processInstanceId,
            @RequestParam String flowNodeId) {

        return Result.OK();
    }
}
