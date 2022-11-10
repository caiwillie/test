package com.brandnewdata.mop.poc.scene.rest;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.scene.dto.SceneDto;
import com.brandnewdata.mop.poc.scene.dto.SceneProcessDto;
import com.brandnewdata.mop.poc.scene.request.ExportReq;
import com.brandnewdata.mop.poc.scene.response.LoadResp;
import com.brandnewdata.mop.poc.scene.service.DataExternalService;
import com.brandnewdata.mop.poc.scene.service.ISceneService;
import com.dxy.library.json.jackson.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

/**
 * 业务场景集成相关的接口
 *
 * @author caiwillie
 */
@Slf4j
@RestController
public class SceneRest {

    @Autowired
    private ISceneService service;

    @Resource
    private DataExternalService dataExternalService;

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
    public Result<Page<SceneDto>> page(
            @RequestParam(required = false) String projectId,
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String name) {
        Page<SceneDto> page = service.page(pageNum, pageSize, name);
        return Result.OK(page);
    }

    /**
     * 详情
     *
     * @param id 场景 id
     * @return the result
     */
    @GetMapping(value = "/rest/businessScene/detail")
    public Result<SceneDto> detail(
            @RequestParam Long id) {
        SceneDto sceneDTO = service.getOne(id);
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
    public Result<SceneDto> save(@RequestBody SceneDto sceneDTO) {
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
    public Result<SceneProcessDto> saveProcessDefinition(
            @RequestBody SceneProcessDto sceneProcessDTO) {
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
            @RequestBody SceneProcessDto sceneProcessDTO) {
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
    public Result deleteProcessDefinition(@RequestBody SceneProcessDto sceneProcessDTO) {
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
    public Result delete(@RequestBody SceneDto sceneDTO) {
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
    @PostMapping("/rest/businessScene/export")
    public void export(HttpServletResponse response, @RequestBody ExportReq req) {
        log.info("path: /rest/businessScene/export, req: {}", JacksonUtil.to(req));

        File file = dataExternalService.export(req);
        InputStream inputStream = FileUtil.getInputStream(file);

        String fileName = StrUtil.format("SceneExport({}).zip",
                LocalDateTimeUtil.format(LocalDateTime.now(), DatePattern.PURE_DATETIME_PATTERN));
        String contentType = ObjectUtil.defaultIfNull(FileUtil.getMimeType(fileName), "application/octet-stream");

        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        ServletUtil.write(response, inputStream, contentType, fileName);
        // 删除文件
        FileUtil.del(file);
    }

    /**
     * 预备导入场景
     *
     * @param file the file
     * @return result
     */
    @PostMapping("/rest/businessScene/prepareLoad")
    public Result<LoadResp> prepareLoad(@RequestParam MultipartFile file) {
        try {
            LoadResp resp = dataExternalService.prepareLoad(file.getBytes());
            return Result.OK(resp);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 确认导入场景
     *
     * @return
     */
    @PostMapping("/rest/businessScene/confirmLoad")
    public Result confirmLoad(@RequestBody LoadResp req) {
        dataExternalService.confirmLoad(req);
        return Result.ok();
    }

}
