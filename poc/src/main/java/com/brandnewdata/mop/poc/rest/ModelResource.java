package com.brandnewdata.mop.poc.rest;

import cn.hutool.core.lang.Assert;
import com.brandnewdata.mop.poc.common.service.result.PageResult;
import com.brandnewdata.mop.poc.common.service.result.Result;
import com.brandnewdata.mop.poc.pojo.entity.DeModelEntity;
import com.brandnewdata.mop.poc.pojo.vo.ModelVo;
import com.brandnewdata.mop.poc.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 模型相关接口
 *
 * @author caiwillie
 */
@RestController(value = "/rest/model")
public class ModelResource {

    @Autowired
    private ModelService modelService;

    @GetMapping(value = "/page")
    public Result<PageResult<ModelVo>> page(
            @RequestParam Integer pageNumber,
            @RequestParam Integer pageSize) {
        PageResult<DeModelEntity> page = modelService.page(pageNumber, pageSize);
        List<ModelVo> records = Optional.ofNullable(page.getRecords()).orElse(Collections.emptyList())
                .stream().map(this::transformToVO).toList();
        PageResult<ModelVo> ret = new PageResult<>(page.getTotal(), records);
        return Result.success(ret);
    }

    @PostMapping(value = "/save")
    public Result<ModelVo> save(@RequestBody ModelVo vo) {
        valid(vo);
        DeModelEntity entity = transformToEntity(vo);
        modelService.save(entity);
        // 回填id
        vo.setId(entity.getId());
        return Result.success(vo);
    }

    @GetMapping(value = "detail")
    public Result<ModelVo> detail(@RequestParam("modelKey") String modelKey) {
        Assert.notNull(modelKey, "模型标识不能为空");
        DeModelEntity entity = modelService.getOne(modelKey);
        Assert.notNull(entity, "模型不存在");
        ModelVo vo = transformToVO(entity);
        return Result.success(vo);
    }

    @PostMapping(value = "/deploy")
    public Result<ModelVo> deploy(@RequestBody ModelVo modelVo) {
        return null;
    }

    private void valid(ModelVo modelVo) {
        Assert.notNull(modelVo.getModelKey(), "模型id不能为空");
        Assert.notNull(modelVo.getName(), "模型名称不能为空");
        Assert.notNull(modelVo.getEditorXML(), "模型定义不能为空");
    }

    private DeModelEntity transformToEntity(ModelVo modelVo) {
        DeModelEntity entity = new DeModelEntity();
        entity.setId(modelVo.getId());
        entity.setName(modelVo.getName());
        entity.setModelKey(modelVo.getModelKey());
        entity.setEditorXml(modelVo.getEditorXML());
        return entity;
    }

    private ModelVo transformToVO(DeModelEntity entity) {
        ModelVo vo = new ModelVo();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setModelKey(entity.getModelKey());
        vo.setEditorXML(entity.getEditorXml());
        return vo;
    }


}
