package com.brandnewdata.mop.poc.scene.rest;

import cn.hutool.core.lang.Assert;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.scene.dto.BusinessSceneDTO;
import com.brandnewdata.mop.poc.scene.dto.BusinessSceneProcessDTO;
import com.brandnewdata.mop.poc.scene.service.IBusinessSceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 业务场景集成相关的接口
 *
 * @author caiwillie
 */
@RestController
public class BusinessSceneRest {

    @Autowired
    private IBusinessSceneService service;

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
    public Result<Page<BusinessSceneDTO>> page(
            @RequestParam String projectId,
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String name) {
        Page<BusinessSceneDTO> page = service.page(pageNum, pageSize, name);
        return Result.OK(page);
    }

    /**
     * 详情
     *
     * @param id 场景 id
     * @return the result
     */
    @GetMapping(value = "/rest/businessScene/detail")
    public Result<BusinessSceneDTO> detail(
            @RequestParam Long id) {
        BusinessSceneDTO businessSceneDTO = service.getOne(id);
        Assert.notNull(businessSceneDTO, "场景 id 不存在");
        return Result.OK(businessSceneDTO);
    }

    /**
     * 保存业务场景
     *
     * @param businessSceneDTO the business scene
     * @return the result
     */
    @PostMapping(value = "/rest/businessScene/save")
    public Result<BusinessSceneDTO> save(@RequestBody BusinessSceneDTO businessSceneDTO) {
        businessSceneDTO = service.save(businessSceneDTO);
        return Result.OK(businessSceneDTO);
    }

    /**
     * 保存业务场景下的流程
     *
     * @param businessSceneProcessDTO the business scene process definition
     * @return the result
     */
    @PostMapping(value = "/rest/businessScene/saveProcessDefinition")
    public Result<BusinessSceneProcessDTO> saveProcessDefinition(
            @RequestBody BusinessSceneProcessDTO businessSceneProcessDTO) {
        businessSceneProcessDTO = service.saveProcessDefinition(businessSceneProcessDTO);
        return Result.OK(businessSceneProcessDTO);
    }

    /**
     * 部署业务场景下的流程
     *
     * @param businessSceneProcessDTO the business scene process definition
     * @return the result
     */
    @PostMapping("/rest/businessScene/deployProcessDefinition")
    public Result deployProcessDefinition (
            @RequestBody BusinessSceneProcessDTO businessSceneProcessDTO) {
        service.deploy(businessSceneProcessDTO);
        return Result.OK();
    }

    /**
     * 删除业务场景下的流程
     *
     * @param businessSceneProcessDTO the business scene process dto
     * @return the result
     */
    @PostMapping(value = "/rest/businessScene/deleteProcessDefinition")
    public Result deleteProcessDefinition(@RequestBody BusinessSceneProcessDTO businessSceneProcessDTO) {
        service.deleteProcessDefinition(businessSceneProcessDTO);
        return Result.OK();
    }

    /**
     * 删除业务场景
     *
     * @param businessSceneDTO the business scene dto
     * @return the result
     */
    @PostMapping(value = "/rest/businessScene/delete")
    public Result delete(@RequestBody BusinessSceneDTO businessSceneDTO) {
        service.delete(businessSceneDTO);
        return Result.OK();
    }

}
