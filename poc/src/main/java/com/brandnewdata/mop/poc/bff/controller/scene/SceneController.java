package com.brandnewdata.mop.poc.bff.controller.scene;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Opt;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.bff.service.scene.SceneBffService;
import com.brandnewdata.mop.poc.bff.vo.env.EnvVo;
import com.brandnewdata.mop.poc.bff.vo.process.ProcessDefinitionVo;
import com.brandnewdata.mop.poc.bff.vo.scene.DebugProcessInstanceVo;
import com.brandnewdata.mop.poc.bff.vo.scene.SceneVersionVo;
import com.brandnewdata.mop.poc.bff.vo.scene.SceneVo;
import com.brandnewdata.mop.poc.bff.vo.scene.VersionProcessVo;
import com.brandnewdata.mop.poc.bff.vo.scene.external.ExportQueryVo;
import com.brandnewdata.mop.poc.bff.vo.scene.external.PrepareLoadVo;
import com.brandnewdata.mop.poc.bff.vo.scene.operate.SceneDeployVo;
import com.brandnewdata.mop.poc.common.dto.Page;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 场景集成相关接口（新）
 *
 * @author caiwillie
 */
@RestController
public class SceneController {

    private final SceneBffService sceneBffService;

    public SceneController(SceneBffService sceneBffService) {
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
     * @param exportQueryVo the export query
     */
    @PostMapping("/rest/scene/export")
    public void export(@RequestBody ExportQueryVo exportQueryVo, HttpServletResponse response) {
        sceneBffService.export(exportQueryVo, response);
    }

    /**
     * 预备导入场景
     */
    @SneakyThrows
    @PostMapping("/rest/scene/load/prepare")
    public Result<PrepareLoadVo> loadPrepare(@RequestParam MultipartFile file) {
        byte[] bytes = file.getBytes();
        PrepareLoadVo ret = sceneBffService.prepareLoad(bytes);
        return Result.OK(ret);
    }

    /**
     * 确认导入场景
     */
    @PostMapping("/rest/scene/load/confirm")
    public void loadConfirm() {
        return;
    }

    /**
     * 获取场景下的版本列表
     * @param sceneId 场景id
     * @return
     */
    @GetMapping(value = "/rest/scene/version/list")
    public Result<List<SceneVersionVo>> listVersion(@RequestParam Long sceneId) {
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
    public Result<List<VersionProcessVo>> listProcess(@RequestParam Long versionId) {
        List<VersionProcessVo> versionProcessVoList = sceneBffService.processList(versionId);
        return Result.OK(versionProcessVoList);
    }

    /**
     * 调试版本
     *
     * @return the result
     */
    @PostMapping(value = "/rest/scene/version/debug")
    public Result<SceneVersionVo> debugVersion(@RequestBody SceneVersionVo vo) {
        SceneVersionVo ret = sceneBffService.debugVersion(vo.getId());
        return Result.OK(ret);
    }

    /**
     * 停止调试
     *
     * @param vo
     * @return
     */
    @PostMapping(value = "/rest/scene/version/debug/stop")
    public Result<SceneVersionVo> stopDebugVersion(@RequestBody SceneVersionVo vo) {
        SceneVersionVo ret = sceneBffService.stopDebugVersion(vo.getId());
        return Result.OK(ret);
    }

    /**
     * 调试记录
     *
     * @param versionId 版本id
     * @param processId 筛选项：流程id
     * @return the result
     */
    @GetMapping(value = "/rest/scene/version/debug/processInstance/list")
    public Result<Page<DebugProcessInstanceVo>> listProcessInstance(@RequestParam Integer pageNum,
                                                                    @RequestParam Integer pageSize,
                                                                    @RequestParam Long versionId,
                                                                    @RequestParam(required = false) String processId) {
        Page<DebugProcessInstanceVo> ret = sceneBffService.listDebugProcessInstance(pageNum, pageSize, versionId);
        return Result.OK(ret);
    }

    /**
     * 获取调试记录所关联的流程定义
     *
     * @param vo the vo
     * @return the result
     */
    @PostMapping(value = "/rest/scene/version/debug/processInstance/definition")
    public Result<ProcessDefinitionVo> definitionDebugProcessInstance(@RequestBody DebugProcessInstanceVo vo) {
        ProcessDefinitionVo ret = sceneBffService.definitionDebugProcessInstance(vo);
        return Result.OK(ret);
    }

    // check new version
    @PostMapping(value = "/rest/scene/version/releaseName/checkNew")
    public Result checkNewReleaseName(@RequestBody SceneVersionVo sceneVersionVo) {
        boolean ret = sceneBffService.checkNewReleaseName(sceneVersionVo.getSceneId(), sceneVersionVo.getVersion());
        return ret ? Result.OK() : Result.error();
    }

    /**
     * 部署新版本
     *
     * @param sceneVersionVo the scene version vo
     * @return the result
     */
    @PostMapping(value = "/rest/scene/version/deploy")
    public Result<SceneVersionVo> deployVersion(@RequestBody SceneVersionVo sceneVersionVo) {
        List<Long> envIdList = Opt.ofNullable(sceneVersionVo.getEnvList()).orElse(ListUtil.empty())
                .stream().map(EnvVo::getId).collect(Collectors.toList());
        SceneVersionVo ret = sceneBffService.deployVersion(sceneVersionVo.getId(), envIdList, sceneVersionVo.getVersion());
        return Result.OK(ret);
    }

    /**
     * 停止某个版本
     *
     * @param sceneVersionVo the scene version vo
     * @return the result
     */
    @PostMapping(value = "/rest/scene/version/stop")
    public Result<SceneVersionVo> versionStop(@RequestBody SceneVersionVo sceneVersionVo) {
        SceneVersionVo ret = sceneBffService.versionStop(sceneVersionVo.getId());
        return Result.OK(ret);
    }

    /**
     * 恢复某个版本
     *
     * @param sceneVersionVo the scene version vo
     * @return the result
     */
    @PostMapping(value = "/rest/scene/version/resume")
    public Result<SceneVersionVo> versionResume(@RequestBody SceneVersionVo sceneVersionVo) {
        List<Long> envIdList = Opt.ofNullable(sceneVersionVo.getEnvList()).orElse(ListUtil.empty())
                .stream().map(EnvVo::getId).collect(Collectors.toList());
        SceneVersionVo ret = sceneBffService.versionResume(sceneVersionVo.getId(), envIdList);
        return Result.OK(ret);
    }

    /**
     * 拷贝至新版本
     *
     * @param oldVersion the old version
     * @return the result
     */
    @PostMapping(value = "/rest/scene/version/copyToNew")
    public Result<SceneVersionVo> versionCopyToNew(@RequestBody SceneVersionVo oldVersion) {
        SceneVersionVo ret = sceneBffService.versionCopyToNew(oldVersion);
        return Result.OK(ret);
    }

    /**
     * 触发新的调试
     *
     * @param vo the vo
     * @return the result
     */
    @PostMapping(value = "/rest/scene/version/process/debug")
    public Result processDebug(@RequestBody VersionProcessVo vo) {
        sceneBffService.processDebug(vo);
        return Result.OK();
    }

    /**
     * 保存版本下的流程
     *
     * @param vo the version process vo
     * @return the result
     */
    @PostMapping(value = "/rest/scene/version/process/save")
    public Result<VersionProcessVo> processSave(@RequestBody VersionProcessVo vo) {
        VersionProcessVo ret = sceneBffService.processSave(vo);
        return Result.OK(ret);
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


    /**
     * 获取所有场景-流程-版本
     *
     * @return the all scene
     */
    @GetMapping("/rest/scene/operate/sceneDeploy/list")
    public Result<List<SceneDeployVo>> getAllScene(@RequestParam Long envId) {
        return Result.OK(sceneBffService.listSceneDeploy(envId));
    }

}
