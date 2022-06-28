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
@RestController
@RequestMapping(value = "/rest/model")
public class ModelResource {

    /**
     * The Model service.
     */
    @Autowired
    private ModelService modelService;

    /**
     * 分页
     *
     * @param pageNumber 分页页码
     * @param pageSize   分页大小
     * @return the result
     */
    @GetMapping(value = "page")
    public Result<PageResult<ModelVo>> page(
            @RequestParam Integer pageNumber,
            @RequestParam Integer pageSize) {
        PageResult<DeModelEntity> page = modelService.page(pageNumber, pageSize);
        List<ModelVo> records = Optional.ofNullable(page.getRecords()).orElse(Collections.emptyList())
                .stream().map(this::transformToVO).collect(Collectors.toList());
        PageResult<ModelVo> ret = new PageResult<>(page.getTotal(), records);
        return Result.success(ret);
    }

    /**
     * 保存（新增或者修改）
     *
     * @param vo the vo
     * @return the result
     */
    @PostMapping(value = "/save")
    public Result<ModelVo> save(@RequestBody ModelVo vo) {
        valid(vo);
        DeModelEntity entity = transformToEntity(vo);
        modelService.save(entity);
        // 返回新增值
        ModelVo ret = transformToVO(entity);
        return Result.success(ret);
    }


    /**
     * 详情
     *
     * @param modelKey the model key
     * @return the result
     */
    @GetMapping(value = "detail")
    public Result<ModelVo> detail(@RequestParam("modelKey") String modelKey) {
        Assert.notNull(modelKey, "模型标识不能为空");
        DeModelEntity entity = modelService.getOne(modelKey);
        Assert.notNull(entity, "模型不存在");
        ModelVo vo = transformToVO(entity);
        return Result.success(vo);
    }

    /**
     * 部署流程
     *
     * @param vo the model vo
     * @return the result
     */
    @PostMapping(value = "/deploy")
    public Result<ModelVo> deploy(@RequestBody ModelVo vo) {
        String editorXML = vo.getEditorXML();
        if(editorXML != null) {
            // 不为空就先保存
            save(vo);
        }

        modelService.deploy(null, null, editorXML);
        return Result.success();
    }

    /**
     * 启动流程
     *
     * @param modelVo the model vo
     * @return the result
     */
    @PostMapping(value = "/start")
    public Result<ModelVo> start(@RequestBody ModelVo modelVo) {
        modelService.start(modelVo.getModelKey());
        return Result.success();
    }

    private void valid(ModelVo modelVo) {
        Assert.notNull(modelVo.getEditorXML(), "模型定义不能为空");
    }


    private DeModelEntity transformToEntity(ModelVo modelVo) {
        DeModelEntity entity = new DeModelEntity();
        entity.setId(modelVo.getId());
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
