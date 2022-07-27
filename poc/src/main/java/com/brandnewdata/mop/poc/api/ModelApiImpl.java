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
import com.brandnewdata.mop.poc.process.ProcessConstants;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinition;
import com.brandnewdata.mop.poc.process.service.IProcessDefinitionService;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService;
import com.brandnewdata.mop.poc.service.ModelService;
import com.brandnewdata.mop.poc.service.ServiceUtil;
import com.dxy.library.json.jackson.JacksonUtil;
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
    @Resource
    private IProcessDeployService deployService;

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
                    ProcessDefinition processDefinition = new ProcessDefinition();
                    processDefinition.setProcessId(trigger.getModelKey());
                    processDefinition.setName(StrUtil.format("【触发器】{}", trigger.getName()));
                    processDefinition.setXml(trigger.getEditorXML());
                    deployService.deploy(processDefinition, ProcessConstants.PROCESS_TYPE_TRIGGER);
                }
            }

            if(CollUtil.isNotEmpty(operates)) {
                for (BPMNResource operate : operates) {
                    ProcessDefinition processDefinition = new ProcessDefinition();
                    processDefinition.setProcessId(operate.getModelKey());
                    processDefinition.setName(StrUtil.format("【操作】{}", operate.getName()));
                    processDefinition.setXml(operate.getEditorXML());
                    deployService.deploy(processDefinition, ProcessConstants.PROCESS_TYPE_OPERATE);
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
            Map<String, Object> result = deployService.startWithResult(processId, requestVariables);
            String response = getResponseVariables(protocol, result);
            return Result.OK(response);
        } catch (Exception e) {
            String response = getExceptionResponse(e);
            return Result.error(response);
        }
    }

    private Map<String, Object> getRequestVariables(String protocol, String content) {
        Map<String, Object> ret = new HashMap<>();

        Object request = null;
        if(StrUtil.equalsAny(protocol, Constants.PROTOCOL_HTTP)) {
            request = JacksonUtil.from(content, HttpRequest.class);
        }
        ret.put("request", request);
        return ret;
    }


    private String getResponseVariables(String protocol, Map<String, Object> result) {
        HttpResponse response = new HttpResponse();
        response.setHeaders(new HashMap<>());
        response.setBody("success");
        return JacksonUtil.to(response);
    }

    private String getExceptionResponse(Exception e) {
        HttpResponse response = new HttpResponse();
        response.setHeaders(new HashMap<>());
        response.setBody(e.getMessage());
        return JacksonUtil.to(response);
    }
}
