package com.brandnewdata.mop.poc.bff.controller.env;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.bff.service.env.EnvBffService;
import com.brandnewdata.mop.poc.bff.vo.env.EnvVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    private EnvBffService envBffService;

    /**
     * 获取环境列表
     *
     * @return
     */
    @GetMapping("/rest/env/list")
    public Result<List<EnvVo>> list() {
        List<EnvVo> envVos = envBffService.list();
        return Result.OK(envVos);
    }

    /**
     * 新增环境
     *
     * @return
     */
    @PostMapping("/rest/env/save")
    public Result<EnvVo> save() {
        return Result.OK();
    }

    /**
     * 停用环境
     *
     * @param envId 环境id
     * @return result
     */
    @GetMapping("/rest/env/stop")
    public Result<EnvVo> stop(@RequestParam Long envId) {
        return Result.OK();
    }
}
