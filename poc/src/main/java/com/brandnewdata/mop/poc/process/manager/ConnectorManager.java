package com.brandnewdata.mop.poc.process.manager;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.connector.api.IConnectorBasicInfoFeign;
import com.brandnewdata.connector.api.IConnectorCommonTriggerProcessConfFeign;
import com.brandnewdata.connector.api.IConnectorConfFeign;
import com.brandnewdata.connector.api.ITriggerProtocolFeign;
import com.brandnewdata.mop.poc.env.service.IEnvService;
import com.brandnewdata.mop.poc.error.ErrorMessage;
import com.brandnewdata.mop.poc.process.manager.dto.ConfigInfo;
import com.brandnewdata.mop.poc.process.manager.dto.ConnectorBasicInfo;
import com.brandnewdata.mop.poc.process.manager.dto.TriggerInfo;
import com.brandnewdata.mop.poc.process.parser.dto.Action;
import com.brandnewdata.mop.poc.scene.dto.SceneReleaseDeployDto;
import com.dxy.library.json.jackson.JacksonUtil;
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

    private final IEnvService envService;

    public ConnectorManager(IEnvService envService) {
        this.envService = envService;
    }

    public ConfigInfo getConfigInfo(String configId) {
        IConnectorConfFeign.ConnectorConfDTO configInfo = confClient.getConfigInfo(Long.parseLong(configId));
        if(configInfo == null) return null;
        ConfigInfo ret = new ConfigInfo();
        ret.setId(configInfo.getId());
        ret.setConnectorGroup(configInfo.getConnectorGroup());
        ret.setConnectorId(configInfo.getConnectorId());
        ret.setConnectorVersion(configInfo.getConnectorVersion());
        ret.setConfigName(configInfo.getConfigName());
        ret.setConfigs(configInfo.getConfigs());
        return ret;
    }

    public String getTriggerXML(Action trigger) {
        IConnectorBasicInfoFeign.ConnectorBasicInfoDTO info =
                basicInfoClient.getInfoById(trigger.getConnectorGroup(), trigger.getConnectorId(), trigger.getConnectorVersion());

        Assert.notNull(info, ErrorMessage.NOT_NULL("触发器"));

        List<TriggerInfo> triggerInfos = Optional
                .ofNullable(JacksonUtil.fromList(info.getConnectorTriggers(), TriggerInfo.class))
                .orElse(ListUtil.empty());

        String triggerId = trigger.getActionId();
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

    public ConnectorBasicInfo getConnectorBasicInfo(String group, String connectorId, String version) {
        IConnectorBasicInfoFeign.ConnectorBasicInfoDTO info = basicInfoClient.getInfoById(group, connectorId, version);
        if(info == null) return null;
        ConnectorBasicInfo ret = new ConnectorBasicInfo();
        ret.setConnectorGroup(info.getConnectorGroup());
        ret.setConnectorId(info.getConnectorId());
        ret.setConnectorName(info.getConnectorName());
        ret.setConnectorVersion(info.getConnectorVersion());
        ret.setConnectorSmallIcon(info.getConnectorSmallIcon());
        return ret;
    }

    public void saveRequestParams(Long envId,
                                  String triggerFullId,
                                  String protocol,
                                  String requestParams,
                                  SceneReleaseDeployDto sceneReleaseDeployDto) {
        IConnectorCommonTriggerProcessConfFeign.ConnectorCommonTriggerProcessConfParamDTO dto =
                new IConnectorCommonTriggerProcessConfFeign.ConnectorCommonTriggerProcessConfParamDTO();
        dto.setEnvId(String.valueOf(envId));
        dto.setProcessId(String.valueOf(sceneReleaseDeployDto.getId()));
        dto.setProcessName(sceneReleaseDeployDto.getProcessName());
        dto.setTriggerFullId(triggerFullId);
        dto.setProtocol(protocol);
        dto.setConfig(requestParams);
        dto.setProcessRelevantInfo(JacksonUtil.to(sceneReleaseDeployDto));
        Integer save = triggerClient.save(dto);
        if(save == null || save != 1) {
            throw new RuntimeException("保存监听配置失败");
        }
    }

    public void resumeVersionProcess(SceneReleaseDeployDto dto) {
        // 1 是生效
        triggerClient.onOrOff(String.valueOf(dto.getId()), 1);
    }

    public void stopVersionProcess(SceneReleaseDeployDto dto) {
        // 2 是禁用
        triggerClient.onOrOff(String.valueOf(dto.getId()), 0);
    }

    public String getProtocol(String connectorId) {
        return protocolClient.getProtocolByConnectorId(connectorId);
    }

}
