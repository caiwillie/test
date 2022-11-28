package com.brandnewdata.mop.poc.bff.controller.operate.scene;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.bff.service.operate.SceneOperateBffService;
import com.brandnewdata.mop.poc.bff.vo.operate.ProcessInstance;
import com.brandnewdata.mop.poc.bff.vo.operate.Statistic;
import com.brandnewdata.mop.poc.bff.vo.operate.condition.Filter;
import com.brandnewdata.mop.poc.bff.vo.operate.condition.Scene;
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
    private SceneOperateBffService sceneOperateBffService;

    /**
     * 获取所有场景-流程-版本
     *
     * @return the all scene
     */
    @GetMapping("/rest/scene/operate/getAllScene")
    public Result<List<Scene>> getAllScene() {
        return Result.OK(sceneOperateBffService.getAllScene());
    }

    /**
     * 获取统计数据
     *
     * @param filter
     * @return
     */
    @PostMapping("/rest/scene/operate/statistic")
    public Result<Statistic> statistic(@RequestBody Filter filter) {
        Statistic statistic = sceneOperateBffService.statistic(filter);
        return Result.OK(statistic);
    }

    /**
     * 分页获取流程实例列表
     * @param filter
     * @return
     */
    @PostMapping("/rest/scene/operate/pageProcessInstance")
    public Result<Page<ProcessInstance>> pageProcessInstance(@RequestBody Filter filter) {
        Page<ProcessInstance> page = sceneOperateBffService.pageProcessInstance(filter);
        return Result.OK(page);
    }
}
