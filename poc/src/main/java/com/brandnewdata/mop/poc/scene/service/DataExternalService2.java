package com.brandnewdata.mop.poc.scene.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.stream.StreamUtil;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.constant.SceneConst;
import com.brandnewdata.mop.poc.process.manager.ConnectorManager;
import com.brandnewdata.mop.poc.process.manager.dto.ConfigInfo;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParser;
import com.brandnewdata.mop.poc.process.parser.dto.Action;
import com.brandnewdata.mop.poc.process.parser.dto.Step1Result;
import com.brandnewdata.mop.poc.process.util.ProcessUtil;
import com.brandnewdata.mop.poc.scene.bo.export.ConfigExportBo;
import com.brandnewdata.mop.poc.scene.bo.export.ConnectorExportBo;
import com.brandnewdata.mop.poc.scene.bo.export.ProcessExportBo;
import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;
import com.brandnewdata.mop.poc.scene.dto.SceneVersionExportDto;
import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataExternalService2 implements IDataExternalService2 {

    private final ISceneVersionService sceneVersionService;

    private final IVersionProcessService versionProcessService;

    private final ConnectorManager connectorManager;

    public DataExternalService2(ISceneVersionService sceneVersionService,
                                IVersionProcessService versionProcessService,
                                ConnectorManager connectorManager) {
        this.sceneVersionService = sceneVersionService;
        this.versionProcessService = versionProcessService;
        this.connectorManager = connectorManager;
    }

    @Override
    public SceneVersionExportDto export(Long versionId, List<String> processIdList) {
        SceneVersionDto sceneVersionDto = sceneVersionService.fetchOneByIdAndCheckStatus(versionId, new int[]{SceneConst.SCENE_VERSION_STATUS__RUNNING,
                SceneConst.SCENE_VERSION_STATUS__STOPPED});
        List<VersionProcessDto> versionProcessDtoList = versionProcessService
                .fetchListByVersionId(ListUtil.of(sceneVersionDto.getId()), false).get(versionId);

        Map<String, File> fileMap = new HashMap<>();

        // 流程定义
        Map<String, ProcessExportBo> processExportBoMap = new LinkedHashMap<>();
        Map<ConnectorExportBo, Map<String, ConfigExportBo>> connectorConfigMap = new LinkedHashMap<>();
        versionProcessDtoList.stream().filter(versionProcessDto -> processIdList.contains(versionProcessDto.getProcessId()))
                .forEach(versionProcessDto -> {
                    ProcessExportBo processExportBo = new ProcessExportBo();
                    String processId = versionProcessDto.getProcessId();
                    String processName = versionProcessDto.getProcessName();
                    String processXml = versionProcessDto.getProcessXml();
                    processExportBo.setProcessId(processId);
                    processExportBo.setProcessName(processName);
                    processExportBo.setProcessXml(processXml);
                    processExportBo.setProcessImg(versionProcessDto.getProcessImg());
                    processExportBoMap.put(processId, processExportBo);

                    // 解析流程中用到的流程
                    Step1Result step1Result = ProcessDefinitionParser.step1(processId, processName, processXml)
                            .parseConfig().step1Result();

                    Map<String, String> configIdMap = step1Result.getConnectorConfigMap();
                    parseConfig(connectorConfigMap, configIdMap);
                });


        connectorConfigMap.forEach((connectorExportBo, configExportBoMap) -> {
           connectorExportBo.setConfigurations(ListUtil.toList(configExportBoMap.values()));
        });


        // 流程配置
        return null;
    }

    private void parseConfig(Map<ConnectorExportBo, Map<String, ConfigExportBo>> configMap, Map<String, String> configIdMap) {
        if(CollUtil.isEmpty(configIdMap)) return;
        Assert.isFalse(CollUtil.hasNull(configMap.keySet()), "配置ID不能为空");
        for (Map.Entry<String, String> entry : configIdMap.entrySet()) {
            String configId = entry.getKey();
            String type = entry.getValue();
            Action action = ProcessUtil.parseAction(type);
            ConfigInfo configInfo = connectorManager.getConfigInfo(configId);

            ConnectorExportBo connectorExportBo = new ConnectorExportBo();
            ConfigExportBo configExportBo = new ConfigExportBo();
            if(configInfo != null) {
                Assert.isTrue(StrUtil.equals(action.getConnectorGroup(), configInfo.getConnectorGroup()));
                Assert.isTrue(StrUtil.equals(action.getConnectorId(), configInfo.getConnectorId()));
                Assert.isTrue(StrUtil.equals(action.getConnectorVersion(), configInfo.getConnectorVersion()));
                connectorExportBo.setConnectorGroup(configInfo.getConnectorGroup());
                connectorExportBo.setConnectorId(configInfo.getConnectorId());
                connectorExportBo.setConnectorVersion(configInfo.getConnectorVersion());
                // todo caiwillie 名称待完善
                connectorExportBo.setConnectorName(configId);

                configExportBo.setConfigName(configInfo.getConfigName());
            } else {
                connectorExportBo.setConnectorGroup(action.getConnectorGroup());
                String connectorId = action.getConnectorId();
                connectorExportBo.setConnectorId(connectorId);
                connectorExportBo.setConnectorVersion(action.getConnectorVersion());
                connectorExportBo.setConnectorName(connectorId);
                configExportBo.setConfigName(configId);
            }

            Map<String, ConfigExportBo> configExportBoMap = configMap.computeIfAbsent(connectorExportBo, k -> new LinkedHashMap<>());

            if(!configExportBoMap.containsKey(configExportBo.getConfigName())) {
                configExportBoMap.put(configExportBo.getConfigName(), configExportBo);
            }

        }

    }


}
