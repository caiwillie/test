package com.brandnewdata.mop.poc.bff.controller;

import com.brandnewdata.common.webresult.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 场景监控相关的接口
 */
@RestController("/rest/scene/operate")
public class SceneOperateController {

    /**
     * 获取所有场景-流程-版本
     *
     * @return the all scene
     */
    @GetMapping("/getAllScene")
    public Result<List<Process>> getAllScene() {

        return Result.OK();
    }

}
