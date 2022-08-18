package com.brandnewdata.mop.poc.operate.rest;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.operate.service.ProcessInstanceService;
import com.brandnewdata.mop.poc.process.dto.ProcessInstance;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 运行监控相关的接口
 *
 */
@RestController
public class ProcessInstanceRest {

    @Resource
    private ProcessInstanceService instanceService;

    /**
     * 获取流程实例分页列表
     *
     * @param deployId 部署id
     * @param pageNum  分页页码
     * @param pageSize 分页大小
     * @return result result
     */
    @GetMapping("/rest/operate/instance/page")
    public Result<Page<ProcessInstance>> page (
            @RequestParam Long deployId,
            @RequestParam int pageNum,
            @RequestParam int pageSize) {
        Page<ProcessInstance> page = instanceService.page(deployId, pageNum, pageSize);
        return Result.OK(page);
    }

    /**
     * 获取流程实例详情
     *
     * @param processInstanceId 流程实例id
     * @return the result
     */
    @GetMapping("/rest/operate/instance/detail")
    public Result<ProcessInstance> detail(@RequestParam String processInstanceId) {
        return null;
    }


}
