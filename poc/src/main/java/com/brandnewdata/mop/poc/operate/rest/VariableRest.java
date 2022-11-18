package com.brandnewdata.mop.poc.operate.rest;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.operate.dto.VariableDto;
import com.brandnewdata.mop.poc.operate.service.VariableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 流程监控相关的接口
 */
@RestController
public class VariableRest {

    @Autowired
    private VariableService variableService;

    /**
     * 获取流程变量列表（根据scopeId）
     *
     * @param processInstanceId 流程实例id
     * @param scopeId           scope id（通常是flowNodeInstanceId）
     * @return the result
     */
    @GetMapping("/rest/operate/process/variable/listByScopeId")
    public Result<List<VariableDto>> listByFlowNodeInstance(
            @RequestParam String processInstanceId, @RequestParam String scopeId) {
        List<VariableDto> list = variableService.listByScopeId(processInstanceId, scopeId);
        return Result.OK(list);
    }

}
