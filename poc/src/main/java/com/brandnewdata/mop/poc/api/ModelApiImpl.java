package com.brandnewdata.mop.poc.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.api.dto.BPMNResource;
import com.brandnewdata.mop.api.ModelApi;
import com.brandnewdata.mop.api.dto.ConnectorResource;
import com.brandnewdata.mop.api.dto.StartMessage;
import com.brandnewdata.mop.poc.parser.XMLDTO;
import com.brandnewdata.mop.poc.parser.XMLParser3;
import com.brandnewdata.mop.poc.service.ModelService;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class ModelApiImpl implements ModelApi {

    @Resource
    private ModelService modelService;

    @Override
    public Result deployConnector(ConnectorResource resource) {

        try {
            Assert.notNull(resource, "连接器资源为空");

            List<BPMNResource> operates = resource.getOperates();

            List<BPMNResource> triggers = resource.getTriggers();

            Assert.isTrue(CollUtil.isNotEmpty(operates) || CollUtil.isNotEmpty(triggers),
                    "操作和触发器为空");

            for (BPMNResource trigger : triggers) {
                String modelKey = trigger.getModelKey();
                String name = trigger.getName();
                XMLDTO xmlDto = new XMLParser3(modelKey, name).parse(trigger.getEditorXML())
                        .replaceGeneralTrigger()
                        .build();
                modelService.deploy(xmlDto.getModelKey(), xmlDto.getName(), xmlDto.getZeebeXML());
            }

            return Result.OK();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Override
    public Result startByConnectorMessages(List<StartMessage> messages) {
        return null;
    }
}
