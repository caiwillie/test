package com.brandnewdata.mop.poc.process.manager;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.connector.api.IConnectorBasicInfoFeign;
import com.brandnewdata.connector.api.IConnectorCommonTriggerProcessConfFeign;
import com.brandnewdata.connector.api.IConnectorConfFeign;
import com.brandnewdata.connector.api.ITriggerProtocolFeign;
import com.brandnewdata.mop.poc.error.ErrorMessage;
import com.brandnewdata.mop.poc.process.manager.dto.ConfigInfo;
import com.brandnewdata.mop.poc.process.manager.dto.ConnectorBasicInfo;
import com.brandnewdata.mop.poc.process.manager.dto.TriggerInfo;
import com.brandnewdata.mop.poc.process.parser.dto.Action;
import com.brandnewdata.mop.poc.scene.dto.SceneReleaseDeployDto;
import com.dxy.library.json.jackson.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
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

    public ConfigInfo getConfigInfo(String configId) {
        IConnectorConfFeign.ConnectorConfDTO configInfo = confClient.getConfigInfo(Long.parseLong(configId));
        log.debug("[api] IConnectorConfFeign#getConfigInfo. configId: {}, result: {}", configId, JacksonUtil.to(configInfo));
        if(configInfo == null) return null;
        return createFrom(configInfo);
    }

    public List<ConfigInfo> listConfigInfo(String connectorGroup, String connectorId, String connectorVersion) {
        List<IConnectorConfFeign.ConnectorConfDTO> configInfoList = confClient.getConfigInfoList(connectorGroup, connectorId, connectorVersion);
        log.debug("[api] IConnectorConfFeign#getConfigInfoList: connectorGroup {}, connectorId {}, connectorVersion {}, result {}",
                connectorGroup, connectorId, connectorVersion, JacksonUtil.to(configInfoList));
        if(CollUtil.isEmpty(configInfoList)) return null;
        return configInfoList.stream().map(this::createFrom).collect(Collectors.toList());
    }

    private  ConfigInfo createFrom(IConnectorConfFeign.ConnectorConfDTO dto) {
        ConfigInfo ret = new ConfigInfo();
        ret.setId(dto.getId());
        ret.setConnectorGroup(dto.getConnectorGroup());
        ret.setConnectorId(dto.getConnectorId());
        ret.setConnectorVersion(dto.getConnectorVersion());
        ret.setConfigName(dto.getConfigName());
        ret.setConfigs(dto.getConfigs());
        return ret;
    }

    public String getTriggerXML(Action trigger) {
        String connectorGroup = trigger.getConnectorGroup();
        String connectorId = trigger.getConnectorId();
        String connectorVersion = trigger.getConnectorVersion();
        IConnectorBasicInfoFeign.ConnectorBasicInfoDTO connectorBasicInfo =
                basicInfoClient.getInfoById(connectorGroup, connectorId, connectorVersion);
        log.debug("[api] IConnectorBasicInfoFeign#getInfoById: connectorGroup {}, connectorId {}, connectorVersion {}, result {}",
                connectorGroup, connectorId, connectorVersion, JacksonUtil.to(connectorBasicInfo));

        Assert.notNull(connectorBasicInfo, ErrorMessage.NOT_NULL("触发器"));
        List<TriggerInfo> triggerInfos = Optional
                .ofNullable(JacksonUtil.fromList(connectorBasicInfo.getConnectorTriggers(), TriggerInfo.class))
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

    public ConnectorBasicInfo getConnectorBasicInfo(String connectorGroup, String connectorId, String connectorVersion) {
        IConnectorBasicInfoFeign.ConnectorBasicInfoDTO connectorBasicInfo = basicInfoClient.getInfoById(connectorGroup, connectorId, connectorVersion);
        log.debug("[api] IConnectorBasicInfoFeign#getInfoById: connectorGroup {}, connectorId {}, connectorVersion {}, result {}",
                connectorGroup, connectorId, connectorVersion, JacksonUtil.to(connectorBasicInfo));

        if(connectorBasicInfo == null) return null;
        ConnectorBasicInfo ret = new ConnectorBasicInfo();
        ret.setConnectorGroup(connectorBasicInfo.getConnectorGroup());
        ret.setConnectorId(connectorBasicInfo.getConnectorId());
        ret.setConnectorName(connectorBasicInfo.getConnectorName());
        ret.setConnectorVersion(connectorBasicInfo.getConnectorVersion());
        ret.setConnectorSmallIcon(connectorBasicInfo.getConnectorSmallIcon());
        ret.setConnectorType(connectorBasicInfo.getConnectorType());
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
        Integer result = triggerClient.onOrOff(String.valueOf(dto.getId()), 1);
    }

    public void stopVersionProcess(SceneReleaseDeployDto dto) {
        // 2 是禁用
        Integer result = triggerClient.onOrOff(String.valueOf(dto.getId()), 0);
    }

    public String getProtocol(String connectorId) {
        return protocolClient.getProtocolByConnectorId(connectorId);
    }

}
