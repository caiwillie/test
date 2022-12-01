package com.brandnewdata.mop.poc.bff.controller.scene;

import cn.hutool.core.collection.ListUtil;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.bff.service.scene.SceneOperateBffService2;
import com.brandnewdata.mop.poc.bff.vo.operate.Statistic;
import com.brandnewdata.mop.poc.bff.vo.process.ProcessDefinitionVo;
import com.brandnewdata.mop.poc.bff.vo.scene.operate.OperateProcessInstanceVo;
import com.brandnewdata.mop.poc.bff.vo.scene.operate.ProcessInstance;
import com.brandnewdata.mop.poc.bff.vo.scene.operate.SceneDeployFilter;
import com.brandnewdata.mop.poc.bff.vo.scene.operate.SceneDeployVo;
import com.brandnewdata.mop.poc.bff.vo.scene.operate.condition.Filter;
import com.brandnewdata.mop.poc.bff.vo.scene.operate.condition.Scene;
import com.brandnewdata.mop.poc.common.dto.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 场景监控相关的接口（新）
 */
@RestController
public class SceneOperateController2 {

    private final SceneOperateBffService2 sceneOperateBffService;

    public SceneOperateController2(SceneOperateBffService2 sceneOperateBffService) {
        this.sceneOperateBffService = sceneOperateBffService;
    }

    /**
     * 分页获取流程实例列表
     * @param filter
     * @return
     */
    @PostMapping("/rest/scene/operate/processInstance/page")
    public Result<Page<OperateProcessInstanceVo>> pageProcessInstance(@RequestBody SceneDeployFilter filter) {
        return Result.OK(new Page<>(0, ListUtil.empty()));
    }

    /**
     * 获取流程实例所关联的流程定义
     * @param vo
     * @return
     */
    @PostMapping("/rest/scene/operate/processInstance/definition")
    public Result<ProcessDefinitionVo> definitionProcessInstance(@RequestBody OperateProcessInstanceVo vo) {
        return Result.OK();
    }

    /**
     * 获取统计数据
     *
     * @param filter
     * @return
     */
    @PostMapping("/rest/scene/operate/statistic")
    public Result<Statistic> statistic(@RequestBody Filter filter) {
        return Result.OK();
    }
}
