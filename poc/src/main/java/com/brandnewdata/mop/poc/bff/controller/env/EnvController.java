package com.brandnewdata.mop.poc.bff.controller.env;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.bff.service.env.EnvService;
import com.brandnewdata.mop.poc.bff.vo.env.EnvVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.List;


/**
 * 环境相关接口
 *
 * @author caiwillie
 */
@RestController
public class EnvController {

    @Resource
    private EnvService envService;

    /**
     * 获取环境列表
     *
     * @return
     */
    @GetMapping("/rest/env/list")
    public Result<List<EnvVo>> list() {
        List<EnvVo> envVos = envService.list();
        return Result.OK(envVos);
    }

}
