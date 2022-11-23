package com.brandnewdata.mop.poc.bff.controller.scene;

import cn.hutool.core.lang.Assert;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.bff.service.scene.SceneBffService;
import com.brandnewdata.mop.poc.bff.vo.scene.SceneVersionVo;
import com.brandnewdata.mop.poc.bff.vo.scene.SceneVo;
import com.brandnewdata.mop.poc.bff.vo.scene.VersionProcessVo;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.scene.dto.SceneDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SceneController2 {

    private final SceneBffService sceneBffService;

    public SceneController2(SceneBffService sceneBffService) {
        this.sceneBffService = sceneBffService;
    }

    /**
     * 获取场景分页列表
     *
     * @param projectId 项目id
     * @param pageNum   分页码
     * @param pageSize  分页大小
     * @param name      名称（模糊搜索）
     * @return the result
     */
    @GetMapping(value = "/rest/scene/page")
    public Result<Page<SceneVo>> page(
            @RequestParam(required = false) String projectId,
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String name) {
        Page<SceneVo> page = sceneBffService.page(pageNum, pageSize, name);
        return Result.OK(page);
    }

    /**
     * 获取场景下的版本列表
     * @param sceneId 场景id
     * @return
     */
    @GetMapping(value = "/rest/scene/version/list")
    public Result<List<SceneVersionVo>> versionList(@RequestParam Long sceneId) {
        List<SceneVersionVo> sceneVersionVoList = sceneBffService.versionList(sceneId);
        return Result.OK(sceneVersionVoList);
    }

    /**
     * 获取版本下的流程列表
     *
     * @param versionId 版本id
     * @return the result
     */
    @GetMapping(value = "/rest/scene/version/process/list")
    public Result<List<VersionProcessVo>> processList(@RequestParam Long versionId) {
        List<VersionProcessVo> versionProcessVoList = sceneBffService.processList(versionId);
        return Result.OK(versionProcessVoList);
    }


}
