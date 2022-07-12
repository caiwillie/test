package com.brandnewdata.mop.poc.api;

import cn.hutool.core.collection.CollUtil;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.api.dto.BPMNResource;
import com.brandnewdata.mop.api.ModelApi;
import com.brandnewdata.mop.api.dto.StartMessage;
import com.brandnewdata.mop.poc.service.ModelService;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class ModelApiImpl implements ModelApi {

    @Resource
    private ModelService modelService;

    @Override
    public Result deploy(List<BPMNResource> bpmnList) {
        if(CollUtil.isEmpty(bpmnList)) {
            return Result.error("流程资源列表为空");
        }

        for (BPMNResource bpmnResourceDTO : bpmnList) {
            try {
                modelService.deploy(bpmnResourceDTO.getModelKey(), bpmnResourceDTO.getName(), bpmnResourceDTO.getEditorXML());
            } catch (Exception e) {
                return Result.error(e.getMessage());
            }
        }

        return Result.OK();
    }

    @Override
    public Result startByMessages(List<StartMessage> messages) {
        return null;
    }
}
