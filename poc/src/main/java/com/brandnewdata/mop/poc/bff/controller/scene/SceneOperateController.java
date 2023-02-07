package com.brandnewdata.mop.poc.bff.controller.scene;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Opt;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.bff.service.scene.SceneOperateBffService;
import com.brandnewdata.mop.poc.bff.vo.process.ProcessDefinitionVo;
import com.brandnewdata.mop.poc.bff.vo.scene.operate.OperateProcessInstanceVo;
import com.brandnewdata.mop.poc.bff.vo.scene.operate.SceneDeployFilter;
import com.brandnewdata.mop.poc.bff.vo.scene.operate.SceneStatistic;
import com.brandnewdata.mop.poc.common.dto.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;


/**
 * 场景监控相关的接口（新）
 */
@RestController
public class SceneOperateController {

    private final SceneOperateBffService sceneOperateBffService;

    public SceneOperateController(SceneOperateBffService sceneOperateBffService) {
        this.sceneOperateBffService = sceneOperateBffService;
    }

    /**
     * 分页获取流程实例列表
     * @param filter
     * @return
     */
    @PostMapping("/rest/scene/operate/processInstance/page")
    public Result<Page<OperateProcessInstanceVo>> pageProcessInstance(@RequestBody SceneDeployFilter filter) {

        LocalDateTime minStartTime = Opt.ofNullable(filter.getStartTime()).map(date -> DateUtil.parse(date).toLocalDateTime()).orElse(LocalDateTime.now().minusDays(1));
        LocalDateTime maxStartTime = Opt.ofNullable(filter.getEndTime()).map(date -> DateUtil.parse(date).toLocalDateTime()).orElse(null);

        //添加校验, 时间间隔一个月内
        if(!checkTimeInterval(minStartTime,maxStartTime)){
            throw new RuntimeException("只能查询近30天的数据");
        }

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

        LocalDateTime minStartTime = Opt.ofNullable(filter.getStartTime()).map(date -> DateUtil.parse(date).toLocalDateTime()).orElse(LocalDateTime.now().minusDays(1));
        LocalDateTime maxStartTime = Opt.ofNullable(filter.getEndTime()).map(date -> DateUtil.parse(date).toLocalDateTime()).orElse(null);

        //添加校验, 时间间隔一个月内
        if(!checkTimeInterval(minStartTime,maxStartTime)){
            throw new RuntimeException("只能查询近30天的数据");
        }

        SceneStatistic ret = sceneOperateBffService.statistic(filter);
        return Result.OK(ret);
    }


    private boolean checkTimeInterval(LocalDateTime min, LocalDateTime max){
        //最小日期小于30天就行
        Duration duration = Duration.between(min,LocalDateTime.now());
        Long monthMillis = 2592000000L;
        Long dMillis = duration.toMillis();
        if(Math.abs(dMillis)>monthMillis){
            return false;
        }
        return true;
    }
}
