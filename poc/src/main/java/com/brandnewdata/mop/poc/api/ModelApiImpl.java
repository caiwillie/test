package com.brandnewdata.mop.poc.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.api.ModelApi;
import com.brandnewdata.mop.api.dto.BPMNResource;
import com.brandnewdata.mop.api.dto.ConnectorResource;
import com.brandnewdata.mop.api.dto.StartMessage;
import com.brandnewdata.mop.poc.process.ProcessConstants;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionDTO;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService;
import com.dxy.library.json.jackson.JacksonUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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
                    ProcessDefinitionDTO processDefinitionDTO = new ProcessDefinitionDTO();
                    processDefinitionDTO.setProcessId(trigger.getModelKey());
                    processDefinitionDTO.setName(StrUtil.format("【触发器】{}", trigger.getName()));
                    processDefinitionDTO.setXml(trigger.getEditorXML());
                    deployService.deploy(processDefinitionDTO, ProcessConstants.PROCESS_TYPE_TRIGGER);
                }
            }

            if(CollUtil.isNotEmpty(operates)) {
                for (BPMNResource operate : operates) {
                    ProcessDefinitionDTO processDefinitionDTO = new ProcessDefinitionDTO();
                    processDefinitionDTO.setProcessId(operate.getModelKey());
                    processDefinitionDTO.setName(StrUtil.format("【操作】{}", operate.getName()));
                    processDefinitionDTO.setXml(operate.getEditorXML());
                    deployService.deploy(processDefinitionDTO, ProcessConstants.PROCESS_TYPE_OPERATE);
                }
            }

            return Result.OK();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @SneakyThrows
    @Override
    public Result startByConnectorMessages(List<StartMessage> messages) {
        if(CollUtil.isEmpty(messages)) {
            return Result.OK();
        }



        StartMessage startMessage = messages.get(0);
        String processId = startMessage.getProcessId();
        String content = startMessage.getContent();
        Map<String, Object> requestVariables = JacksonUtil.fromMap(content);
        Map<String, Object> result = deployService.startWithResult(processId, requestVariables);
        return Result.OK(JacksonUtil.to(result));
    }
}
