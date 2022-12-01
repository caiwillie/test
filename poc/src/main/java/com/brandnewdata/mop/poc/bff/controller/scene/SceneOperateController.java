package com.brandnewdata.mop.poc.bff.controller.scene;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.bff.service.scene.SceneOperateBffService;
import com.brandnewdata.mop.poc.bff.vo.operate.Statistic;
import com.brandnewdata.mop.poc.bff.vo.scene.operate.ProcessInstance;
import com.brandnewdata.mop.poc.bff.vo.scene.operate.condition.Filter;
import com.brandnewdata.mop.poc.bff.vo.scene.operate.condition.Scene;
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
     * 获取所有场景-流程-版本（废弃）
     *
     * @return the all scene
     */
    @GetMapping("/rest/scene/operate/getAllScene")
    public Result<List<Scene>> getAllScene() {
        return Result.OK(sceneOperateBffService.getAllScene());
    }

    /**
     * 分页获取流程实例列表（废弃）
     * @param filter
     * @return
     */
    @PostMapping("/rest/scene/operate/pageProcessInstance")
    public Result<Page<ProcessInstance>> pageProcessInstance(@RequestBody Filter filter) {
        Page<ProcessInstance> page = sceneOperateBffService.pageProcessInstance(filter);
        return Result.OK(page);
    }
}
