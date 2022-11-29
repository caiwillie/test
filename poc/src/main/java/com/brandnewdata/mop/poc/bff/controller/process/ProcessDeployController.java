package com.brandnewdata.mop.poc.bff.controller.process;

import cn.hutool.core.lang.Assert;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.process.dto.ProcessDeployDto;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 流程部署相关接口
 */
@RestController
public class ProcessDeployController {

    @Resource
    private IProcessDeployService deployService;

    /**
     * 获取流程部署的详情
     *
     * @param id 部署 id
     * @return the result
     */
    @GetMapping("/rest/process/deploy/detail")
    public Result<ProcessDeployDto> detail(@RequestParam long id) {
        ProcessDeployDto deploy = deployService.getOne(id);
        Assert.notNull(deploy, "流程部署 {} 不存在", id);
        return Result.OK(deploy);
    }


}
