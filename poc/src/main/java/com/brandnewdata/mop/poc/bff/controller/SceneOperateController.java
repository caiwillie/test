package com.brandnewdata.mop.poc.bff.controller;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.bff.model.sceneOperate.Statistic;
import com.brandnewdata.mop.poc.bff.model.sceneOperate.condition.Filter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    /**
     * 获取统计数据
     *
     * @param filter
     * @return
     */
    @PostMapping("/statistic")
    public Result<Statistic> statistic(Filter filter) {
        return Result.OK();
    }

}
