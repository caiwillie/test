package com.brandnewdata.mop.poc.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.api.ModelApi;
import com.brandnewdata.mop.api.dto.BPMNResource;
import com.brandnewdata.mop.api.dto.ConnectorResource;
import com.brandnewdata.mop.api.dto.StartMessage;
import com.brandnewdata.mop.api.dto.protocol.request.HttpRequest;
import com.brandnewdata.mop.api.dto.protocol.response.HttpResponse;
import com.brandnewdata.mop.poc.common.Constants;
import com.brandnewdata.mop.poc.parser.XMLDTO;
import com.brandnewdata.mop.poc.parser.XMLParser3;
import com.brandnewdata.mop.poc.service.ModelService;
import com.brandnewdata.mop.poc.service.ServiceUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class ModelApiImpl implements ModelApi {

    private ObjectMapper om = ServiceUtil.OM;
    private MapType mapType = ServiceUtil.MAP_TYPE;

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


            if(CollUtil.isNotEmpty(triggers)) {
                for (BPMNResource trigger : triggers) {
                    String modelKey = trigger.getModelKey();
                    String name = trigger.getName();
                    String xml = trigger.getEditorXML();
                    modelService.deploy(modelKey, name, xml, Constants.TRIGGER_TYPE_GENERAL);
                }
            }

            if(CollUtil.isNotEmpty(operates)) {
                for (BPMNResource operate : operates) {
                    String modelKey = operate.getModelKey();
                    String name = operate.getName();
                    String xml = operate.getEditorXML();
                    modelService.deploy(modelKey, name, xml, Constants.TRIGGER_TYPE_GENERAL);
                }
            }

            return Result.OK();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @Override
    public Result startByConnectorMessages(List<StartMessage> messages) {
        if(CollUtil.isEmpty(messages)) {
            return Result.OK();
        }

        try {
            StartMessage startMessage = messages.get(0);
            String processId = startMessage.getProcessId();
            String protocol = startMessage.getProtocol();
            String content = startMessage.getContent();
            Map<String, Object> requestVariables = getRequestVariables(protocol, content);
            Map<String, Object> result = modelService.startWithResult(processId, requestVariables);
            String response = getResponseVariables(protocol, result);
            return Result.OK(response);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private Map<String, Object> getRequestVariables(String protocol, String content) throws JsonProcessingException {
        Map<String, Object> ret = new HashMap<>();

        Object request = null;
        if(StrUtil.equalsAny(protocol, Constants.PROTOCOL_HTTP)) {
            request = om.readValue(content, HttpRequest.class);
        }
        ret.put("request", request);
        return ret;
    }


    private String getResponseVariables(String protocol, Map<String, Object> result) throws JsonProcessingException {
        HttpResponse response = new HttpResponse();
        response.setHeaders(new HashMap<>());
        response.setBody("success");
        return om.writeValueAsString(response);
    }
}
