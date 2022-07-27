package com.brandnewdata.mop.poc.operate.rest;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.process.dto.ProcessDeploy;

import com.brandnewdata.mop.poc.process.dto.ProcessInstance;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService;
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
    private IProcessDeployService deployService;

    /**
     * 流程部署分页列表
     *
     * @param pageNum 分页页码
     * @param pageSize 分页大小
     * @return
     */
    @GetMapping("/rest/operate/deploy/page")
    public Result<Page<ProcessDeploy>> processDeployPage(
            @RequestParam int pageNum,
            @RequestParam int pageSize) {
        Page<ProcessDeploy> page = deployService.page(pageNum, pageSize);
        return Result.OK(page);
    }

    /**
     * 流程实例分页列表
     *
     * @param pageNum 分页页码
     * @param pageSize 分页大小
     * @return
     */
    @GetMapping("/rest/operate/instance/page")
    public Result<Page<ProcessInstance>> processInstancePage(
            @RequestParam int pageNum,
            @RequestParam int pageSize) {
        return null;
    }

}
