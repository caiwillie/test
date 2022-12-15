package com.brandnewdata.mop.poc.bff.controller.scene;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.bff.service.scene.SceneOperateBffService2;
import com.brandnewdata.mop.poc.bff.vo.process.ProcessDefinitionVo;
import com.brandnewdata.mop.poc.bff.vo.scene.operate.OperateProcessInstanceVo;
import com.brandnewdata.mop.poc.bff.vo.scene.operate.SceneDeployFilter;
import com.brandnewdata.mop.poc.bff.vo.scene.operate.SceneStatistic;
import com.brandnewdata.mop.poc.common.dto.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


/**
 * 场景监控相关的接口（新）
 */
@RestController
public class SceneOperateController {

    private final SceneOperateBffService2 sceneOperateBffService;

    public SceneOperateController(SceneOperateBffService2 sceneOperateBffService) {
        this.sceneOperateBffService = sceneOperateBffService;
    }

    /**
     * 分页获取流程实例列表
     * @param filter
     * @return
     */
    @PostMapping("/rest/scene/operate/processInstance/page")
    public Result<Page<OperateProcessInstanceVo>> pageProcessInstance(@RequestBody SceneDeployFilter filter) {
        Page<OperateProcessInstanceVo> ret = sceneOperateBffService.pageProcessInstance(filter);
        return Result.OK(ret);
    }

    /**
     * 获取流程实例所关联的流程定义
     * @param vo
     * @return
     */
    @PostMapping("/rest/scene/operate/processInstance/definition")
    public Result<ProcessDefinitionVo> definitionProcessInstance(@RequestBody OperateProcessInstanceVo vo) {
        ProcessDefinitionVo ret = sceneOperateBffService.definitionProcessInstance(vo);
        return Result.OK(ret);
    }

    /**
     * 获取统计数据
     *
     * @param filter
     * @return
     */
    @PostMapping("/rest/scene/operate/statistic")
    public Result<SceneStatistic> statistic(@RequestBody SceneDeployFilter filter) {
        SceneStatistic ret = sceneOperateBffService.statistic(filter);
        return Result.OK(ret);
    }
}
