package com.brandnewdata.mop.poc.api;

import cn.hutool.core.collection.CollUtil;
import com.brandnewdata.mop.poc.common.service.result.Result;
import com.brandnewdata.mop.poc.service.ModelService;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class ModelApiImpl implements ModelApi{

    @Resource
    private ModelService modelService;

    @Override
    public Result deploy(List<BPMNResource> bpmnList) {
        if(CollUtil.isEmpty(bpmnList)) {
            return Result.error().setMessage("流程资源列表为空");
        }

        for (BPMNResource bpmnResource : bpmnList) {
            try {
                modelService.deploy(bpmnResource.getModelKey(), bpmnResource.getName(), bpmnResource.getEditorXML());
            } catch (Exception e) {
                return Result.error().setMessage(e.getMessage());
            }
        }

        return Result.success();
    }

}
