package com.brandnewdata.mop.poc.bff.controller;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.bff.model.sceneOperate.ProcessInstance;
import com.brandnewdata.mop.poc.bff.model.sceneOperate.Statistic;
import com.brandnewdata.mop.poc.bff.model.sceneOperate.condition.Filter;
import com.brandnewdata.mop.poc.bff.model.sceneOperate.condition.Scene;
import com.brandnewdata.mop.poc.bff.service.sceneOperate.SceneOperateService;
import com.brandnewdata.mop.poc.common.dto.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;


/**
 * 场景监控相关的接口
 */
@RestController
public class SceneOperateController {

    @Resource
    private SceneOperateService sceneOperateService;

    /**
     * 获取所有场景-流程-版本
     *
     * @return the all scene
     */
    @GetMapping("/rest/scene/operate/getAllScene")
    public Result<List<Scene>> getAllScene() {
        return Result.OK(sceneOperateService.getAllScene());
    }

    /**
     * 获取统计数据
     *
     * @param filter
     * @return
     */
    @PostMapping("/rest/scene/operate/statistic")
    public Result<Statistic> statistic(@RequestBody Filter filter) {
        Statistic statistic = sceneOperateService.statistic(filter);
        return Result.OK(statistic);
    }

    /**
     * 分页获取流程实例列表
     * @param filter
     * @return
     */
    @PostMapping("/rest/scene/operate/pageProcessInstance")
    public Result<Page<ProcessInstance>> pageProcessInstance(@RequestBody Filter filter) {
        Page<ProcessInstance> page = sceneOperateService.pageProcessInstance(filter);
        return Result.OK(page);
    }
}
