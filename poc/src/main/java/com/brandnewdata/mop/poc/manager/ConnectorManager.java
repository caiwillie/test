package com.brandnewdata.mop.poc.manager;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.connector.api.IConnectorBasicInfoFeign;
import com.brandnewdata.connector.api.IConnectorCommonTriggerProcessConfFeign;
import com.brandnewdata.connector.api.IConnectorConfFeign;
import com.brandnewdata.connector.api.ITriggerProtocolFeign;
import com.brandnewdata.mop.poc.error.ErrorMessage;
import com.brandnewdata.mop.poc.manager.dto.TriggerInfo;
import com.brandnewdata.mop.poc.process.dto.parser.TriggerOrOperate;
import com.brandnewdata.mop.poc.process.dto.parser.TriggerProcessDefinitionDto;
import com.dxy.library.json.jackson.JacksonUtil;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Component
public class ConnectorManager {

    @Resource
    private IConnectorCommonTriggerProcessConfFeign triggerClient;

    @Resource
    private IConnectorConfFeign confClient;

    @Resource
    private IConnectorBasicInfoFeign basicInfoClient;

    @Resource
    private ITriggerProtocolFeign protocolClient;

    public String getProperties(String id) {
        IConnectorConfFeign.ConnectorConfDTO configInfo = confClient.getConfigInfo(Long.parseLong(id));
        return configInfo.getConfigs();
    }

    public String getTriggerXML(TriggerOrOperate trigger) {
        IConnectorBasicInfoFeign.ConnectorBasicInfoDTO info =
                basicInfoClient.getInfoById(trigger.getGroupId(), trigger.getConnectorId(), trigger.getVersion());

        Assert.notNull(info, ErrorMessage.NOT_NULL("触发器"));

        List<TriggerInfo> triggerInfos = Optional
                .ofNullable(JacksonUtil.fromList(info.getConnectorTriggers(), TriggerInfo.class))
                .orElse(ListUtil.empty());

        String triggerId = trigger.getTriggerOrOperateId();
        String xml = null;

        for (TriggerInfo triggerInfo : triggerInfos) {
            String id = triggerInfo.getTriggerName();
            if(StrUtil.equals(triggerId, id)) {
                // 从列表中取出 xml
                xml = triggerInfo.getProcessEditing();
                break;
            }
        }

        return xml;
    }

    @SneakyThrows
    public void saveRequestParams(TriggerProcessDefinitionDto processDefinition) {
        IConnectorCommonTriggerProcessConfFeign.ConnectorCommonTriggerProcessConfParamDTO dto =
                new IConnectorCommonTriggerProcessConfFeign.ConnectorCommonTriggerProcessConfParamDTO();
        dto.setProcessId(processDefinition.getProcessId());
        dto.setProcessName(processDefinition.getName());
        dto.setProtocol(processDefinition.getProtocol());
        dto.setConfig(JacksonUtil.to(processDefinition.getRequestParams()));
        dto.setTriggerFullId(processDefinition.getTrigger().getFullId());
        Integer success = triggerClient.save(dto);
        if(success == null || success != 1) {
            throw new RuntimeException("保存监听配置失败");
        }

    }

    public String getProtocol(String connectorId) {
        return protocolClient.getProtocolByConnectorId(connectorId);
    }

}
