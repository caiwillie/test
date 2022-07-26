package com.brandnewdata.mop.poc.operate.rest;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.operate.dto.ProcessInstance;
import com.brandnewdata.mop.poc.operate.service.OperateService;
import com.brandnewdata.mop.poc.process.dto.ProcessDeploy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 运行监控相关的接口
 *
 */
@RestController
public class OperateRest {

    @Resource
    private OperateService service;

    /**
     * 流程部署分页列表
     *
     * @param pageNum 分页页码
     * @param pageSize 分页大小
     * @return
     */
    @GetMapping("/rest/operate/processDeploy/page")
    public Result<Page<ProcessDeploy>> processDeployPage(
            @RequestParam int pageNum,
            @RequestParam int pageSize) {
        Page<ProcessDeploy> page = service.processDefinitionPage(pageNum, pageSize);
        return Result.OK(page);
    }

    /**
     * 流程实例分页列表
     *
     * @param pageNum 分页页码
     * @param pageSize 分页大小
     * @return
     */
    @GetMapping("/rest/operate/processInstance/page")
    public Result<Page<ProcessInstance>> processInstancePage(
            @RequestParam int pageNum,
            @RequestParam int pageSize) {
        return null;
    }

}
