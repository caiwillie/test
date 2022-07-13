package com.brandnewdata.mop.poc.api;

import cn.hutool.core.collection.CollUtil;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.api.dto.BPMNResource;
import com.brandnewdata.mop.api.ModelApi;
import com.brandnewdata.mop.api.dto.ConnectorResource;
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
    public Result deployConnector(ConnectorResource bpmnList) {
        return null;
    }

    @Override
    public Result startByConnectorMessages(List<StartMessage> messages) {
        return null;
    }
}
