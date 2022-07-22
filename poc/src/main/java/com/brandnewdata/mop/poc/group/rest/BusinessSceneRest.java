package com.brandnewdata.mop.poc.group.rest;

import cn.hutool.core.lang.Assert;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.group.dto.BusinessScene;
import com.brandnewdata.mop.poc.group.dto.BusinessSceneProcessDefinition;
import com.brandnewdata.mop.poc.group.service.IBusinessSceneService;
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
     * @param pageNum  分页码
     * @param pageSize 分页大小
     * @return the result
     */
    @GetMapping(value = "/rest/businessScene/page")
    public Result<Page<BusinessScene>> page(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize) {
        Page<BusinessScene> page = service.page(pageNum, pageSize);
        return Result.OK(page);
    }

    /**
     * 详情
     *
     * @param id 场景 id
     * @return the result
     */
    @GetMapping(value = "/rest/businessScene/detail")
    public Result<BusinessScene> detail(
            @RequestParam Long id) {
        BusinessScene businessScene = service.detail(id);
        Assert.notNull(businessScene, "场景 id 不存在");
        return Result.OK(businessScene);
    }


    /**
     * 保存业务场景
     *
     * @param businessScene the business scene
     * @return the result
     */
    @PostMapping(value = "/rest/businessScene/save")
    public Result<BusinessScene> save(@RequestBody BusinessScene businessScene) {
        businessScene = service.save(businessScene);
        return Result.OK(businessScene);
    }

    /**
     * 保存业务场景下的流程
     *
     * @param businessSceneProcessDefinition the business scene process definition
     * @return the result
     */
    @PostMapping(value = "/rest/businessScene/saveProcessDefinition")
    public Result<BusinessSceneProcessDefinition> saveProcessDefinition(@RequestBody BusinessSceneProcessDefinition businessSceneProcessDefinition) {
        businessSceneProcessDefinition = service.saveProcessDefinition(businessSceneProcessDefinition);
        return Result.OK(businessSceneProcessDefinition);
    }

}
