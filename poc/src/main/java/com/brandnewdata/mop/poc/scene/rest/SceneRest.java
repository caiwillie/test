package com.brandnewdata.mop.poc.scene.rest;

import cn.hutool.core.lang.Assert;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.scene.dto.SceneDTO;
import com.brandnewdata.mop.poc.scene.dto.SceneProcessDTO;
import com.brandnewdata.mop.poc.scene.request.ExportReq;
import com.brandnewdata.mop.poc.scene.service.ISceneService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 业务场景集成相关的接口
 *
 * @author caiwillie
 */
@RestController
public class SceneRest {

    @Autowired
    private ISceneService service;

    /**
     * 分页列表
     *
     * @param projectId 项目id
     * @param pageNum   分页码
     * @param pageSize  分页大小
     * @param name      名称（模糊搜索）
     * @return the result
     */
    @GetMapping(value = "/rest/businessScene/page")
    public Result<Page<SceneDTO>> page(
            @RequestParam(required = false) String projectId,
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String name) {
        Page<SceneDTO> page = service.page(pageNum, pageSize, name);
        return Result.OK(page);
    }

    /**
     * 详情
     *
     * @param id 场景 id
     * @return the result
     */
    @GetMapping(value = "/rest/businessScene/detail")
    public Result<SceneDTO> detail(
            @RequestParam Long id) {
        SceneDTO sceneDTO = service.getOne(id);
        Assert.notNull(sceneDTO, "场景 id 不存在");
        return Result.OK(sceneDTO);
    }

    /**
     * 保存业务场景
     *
     * @param sceneDTO the business scene
     * @return the result
     */
    @PostMapping(value = "/rest/businessScene/save")
    public Result<SceneDTO> save(@RequestBody SceneDTO sceneDTO) {
        sceneDTO = service.save(sceneDTO);
        return Result.OK(sceneDTO);
    }

    /**
     * 保存业务场景下的流程
     *
     * @param sceneProcessDTO the business scene process definition
     * @return the result
     */
    @PostMapping(value = "/rest/businessScene/saveProcessDefinition")
    public Result<SceneProcessDTO> saveProcessDefinition(
            @RequestBody SceneProcessDTO sceneProcessDTO) {
        sceneProcessDTO = service.saveProcessDefinition(sceneProcessDTO);
        return Result.OK(sceneProcessDTO);
    }

    /**
     * 部署业务场景下的流程
     *
     * @param sceneProcessDTO the business scene process definition
     * @return the result
     */
    @PostMapping("/rest/businessScene/deployProcessDefinition")
    public Result deployProcessDefinition (
            @RequestBody SceneProcessDTO sceneProcessDTO) {
        service.deploy(sceneProcessDTO);
        return Result.OK();
    }

    /**
     * 删除业务场景下的流程
     *
     * @param sceneProcessDTO the business scene process dto
     * @return the result
     */
    @PostMapping(value = "/rest/businessScene/deleteProcessDefinition")
    public Result deleteProcessDefinition(@RequestBody SceneProcessDTO sceneProcessDTO) {
        service.deleteProcessDefinition(sceneProcessDTO);
        return Result.OK();
    }

    /**
     * 删除业务场景
     *
     * @param sceneDTO the business scene dto
     * @return the result
     */
    @PostMapping(value = "/rest/businessScene/delete")
    public Result delete(@RequestBody SceneDTO sceneDTO) {
        service.delete(sceneDTO);
        return Result.OK();
    }

    /**
     * 导出场景
     *
     * @param response the response
     * @param req      the req
     * @throws IOException the io exception
     */
    @PostMapping("/export")
    public void export(
            HttpServletResponse response,
            @RequestBody ExportReq req) throws IOException {
    }

    /**
     * 导入场景
     *
     * @param file the file
     * @return result
     */
    @ApiOperation(value = "导入数据")
    @PostMapping("/load")
    public Result load(@RequestParam MultipartFile file) {
        return Result.OK();
    }

}
