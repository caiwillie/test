package com.brandnewdata.mop.poc.operate.rest;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.process.dto.ProcessDeploy;

import com.brandnewdata.mop.poc.process.dto.ProcessInstance;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 运行监控相关的接口
 *
 */
@RestController
public class DeployRest {

    @Resource
    private IProcessDeployService deployService;

    /**
     * 流程部署分页列表
     *
     * @param pageNum  分页页码
     * @param pageSize 分页大小
     * @return result
     */
    @GetMapping("/rest/operate/deploy/page")
    public Result<Page<ProcessDeploy>> page (
            @RequestParam int pageNum,
            @RequestParam int pageSize) {
        Page<ProcessDeploy> page = deployService.page(pageNum, pageSize);
        return Result.OK(page);
    }

}