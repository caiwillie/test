package com.brandnewdata.mop.poc.bff.controller.scene;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.bff.service.scene.SceneBffService;
import com.brandnewdata.mop.poc.bff.vo.scene.SceneVersionVo;
import com.brandnewdata.mop.poc.bff.vo.scene.SceneVo;
import com.brandnewdata.mop.poc.bff.vo.scene.VersionProcessVo;
import com.brandnewdata.mop.poc.bff.vo.scene.external.ExportQuery;
import com.brandnewdata.mop.poc.common.dto.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 场景集成相关接口（新）
 *
 * @author caiwillie
 */
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
     * 保存场景
     *
     * @return
     */
    @PostMapping(value = "/rest/scene/save")
    public Result<SceneVo> save(@RequestBody SceneVo sceneVo) {
        SceneVo ret = sceneBffService.save(sceneVo);
        return Result.OK(ret);
    }

    /**
     * 删除场景
     *
     * @param sceneVo the scene vo
     * @return the result
     */
    @PostMapping(value = "/rest/scene/delete")
    public Result<SceneVo> delete(@RequestBody SceneVo sceneVo) {
        return Result.OK();
    }

    /**
     * 导出场景
     *
     * @param exportQuery the export query
     */
    @PostMapping("/rest/scene/export")
    public void export(@RequestBody ExportQuery exportQuery) {
        return;
    }

    /**
     * 预备导入场景
     */
    @PostMapping("/rest/scenne/load/prepare")
    public void loadPrepare() {
        return;
    }

    /**
     * 确认导入场景
     */
    @PostMapping("/rest/scenne/load/confirm")
    public void loadConfirm() {
        return;
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

    @PostMapping(value = "/rest/scene/version/debug")
    public Result<SceneVersionVo> versionDebug(@RequestBody SceneVersionVo sceneVersionVo) {
        return Result.OK();
    }

    /**
     * 部署新版本
     *
     * @param sceneVersionVo the scene version vo
     * @return the result
     */
    @PostMapping(value = "/rest/scene/version/deploy")
    public Result<SceneVersionVo> versionDeploy(@RequestBody SceneVersionVo sceneVersionVo) {
        return Result.OK();
    }

    /**
     * 停止某个版本
     *
     * @param sceneVersionVo the scene version vo
     * @return the result
     */
    @PostMapping(value = "/rest/scene/version/stop")
    public Result<SceneVersionVo> versionStop(@RequestBody SceneVersionVo sceneVersionVo) {
        return Result.OK();
    }

    /**
     * 恢复某个版本
     *
     * @param sceneVersionVo the scene version vo
     * @return the result
     */
    @PostMapping(value = "/rest/scene/version/resume")
    public Result<SceneVersionVo> versionResume(@RequestBody SceneVersionVo sceneVersionVo) {
        return Result.OK();
    }

    /**
     * 拷贝至新版本
     *
     * @param oldVersion the old version
     * @return the result
     */
    @PostMapping(value = "/rest/scene/version/copyToNew")
    public Result<SceneVersionVo> versionCopyToNew(@RequestBody SceneVersionVo oldVersion) {
        return Result.OK();
    }

    /**
     * 保存版本下的流程
     *
     * @param versionProcessVo the version process vo
     * @return the result
     */
    @PostMapping(value = "/rest/scene/version/process/save")
    public Result<VersionProcessVo> processSave(@RequestBody VersionProcessVo versionProcessVo) {
        return Result.OK();
    }

    /**
     * 删除版本下的流程
     *
     * @param versionProcessVo the version process vo
     * @return the result
     */
    @PostMapping(value = "/rest/scene/version/process/delete")
    public Result processDelete(@RequestBody VersionProcessVo versionProcessVo) {
        return Result.OK();
    }


}
