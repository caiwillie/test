package com.brandnewdata.mop.poc.scene.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
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
import com.brandnewdata.mop.poc.scene.bo.export.SceneExportFileBo;
import com.brandnewdata.mop.poc.scene.converter.ConnectorConfigDtoConverter;
import com.brandnewdata.mop.poc.scene.dao.SceneLoadDao;
import com.brandnewdata.mop.poc.scene.dto.*;
import com.brandnewdata.mop.poc.scene.dto.external.ConfirmLoadDto;
import com.brandnewdata.mop.poc.scene.dto.external.ConnectorConfigDto;
import com.brandnewdata.mop.poc.scene.dto.external.PrepareLoadDto;
import com.brandnewdata.mop.poc.scene.po.SceneLoadPo;
import com.dxy.library.json.jackson.JacksonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
public class DataExternalService2 implements IDataExternalService2 {

    private static final String FILENAME__PROCESS_DEFINITION = "process_definition.json";

    private static final String FILENAME__CONFIG = "config.json";

    private final ISceneService2 sceneService;
    private final ISceneVersionService sceneVersionService;

    private final IVersionProcessService versionProcessService;

    private final ConnectorManager connectorManager;

    private static final ObjectMapper OM = JacksonUtil.getObjectMapper();

    private static final CollectionType PROCESS_DEFINITION_TYPE = OM.getTypeFactory().constructCollectionType(List.class, ProcessExportBo.class);

    private static final CollectionType CONNECTOR_CONFIG_TYPE = OM.getTypeFactory().constructCollectionType(List.class, ConnectorExportBo.class);

    @Resource
    private SceneLoadDao sceneLoadDao;

    public DataExternalService2(ISceneService2 sceneService,
                                ISceneVersionService sceneVersionService,
                                IVersionProcessService versionProcessService,
                                ConnectorManager connectorManager) {
        this.sceneService = sceneService;
        this.sceneVersionService = sceneVersionService;
        this.versionProcessService = versionProcessService;
        this.connectorManager = connectorManager;
    }

    @Override
    public SceneVersionExportDto export(Long versionId, List<String> processIdList) {
        SceneVersionDto sceneVersionDto = sceneVersionService.fetchOneByIdAndCheckStatus(versionId, new int[]{SceneConst.SCENE_VERSION_STATUS__RUNNING,
                SceneConst.SCENE_VERSION_STATUS__STOPPED});

        Long sceneId = sceneVersionDto.getSceneId();
        SceneDto2 sceneDto = sceneService.fetchById(ListUtil.of(sceneId)).get(sceneId);

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

        fileMap.put(FILENAME__PROCESS_DEFINITION, createTempFile(JacksonUtil.to(processExportBoMap.values())));

        fileMap.put(FILENAME__CONFIG, createTempFile(JacksonUtil.to(connectorConfigMap.keySet())));
        File dir = createTempDir(fileMap, true);
        File zip = ZipUtil.zip(dir);

        SceneVersionExportDto ret = new SceneVersionExportDto();
        ret.setFileName(StrUtil.format("【场景导出】{}-{}.zip", sceneDto.getName(), sceneVersionDto.getVersion()));
        byte[] bytes = IoUtil.readBytes(FileUtil.getInputStream(zip));
        ret.setBytes(bytes);
        return ret;
    }

    @Override
    public PrepareLoadDto prepareLoad(byte[] bytes) {
        PrepareLoadDto ret = new PrepareLoadDto();
        Long id = saveBytes(bytes);
        ret.setId(id);

        SceneExportFileBo sceneExportFileBo = parseBytes(bytes);
        List<ConnectorExportBo> connectorExportBoList = sceneExportFileBo.getConnectorExportBoList();
        List<ConnectorConfigDto> connectorConfigDtos = new ArrayList<>();
        for (ConnectorExportBo connectorExportBo : connectorExportBoList) {
            for (ConfigExportBo configExportBo : connectorExportBo.getConfigurations()) {
                ConnectorConfigDto configDto = ConnectorConfigDtoConverter.createFrom(connectorExportBo, configExportBo);
                connectorConfigDtos.add(configDto);
            }
        }
        ret.setConfigs(connectorConfigDtos);
        return ret;
    }

    @Override
    public SceneVersionDto confirmLoad(ConfirmLoadDto confirmLoadDto) {
        Long id = confirmLoadDto.getId();
        String newSceneName = confirmLoadDto.getNewSceneName();
        Assert.notNull(id);
        Assert.notNull(newSceneName);

        // 保存场景下的流程
        SceneLoadPo sceneLoadPo = sceneLoadDao.selectById(id);
        Assert.notNull(sceneLoadPo);
        SceneExportFileBo sceneExportFileBo = parseBytes(sceneLoadPo.getZipBytes());

        // 保存场景
        SceneDto2 sceneDto = new SceneDto2();
        sceneDto.setName(newSceneName);
        sceneDto = sceneService.save(sceneDto);

        // 新增初始化版本
        SceneVersionDto sceneVersionDto = new SceneVersionDto();
        sceneVersionDto.setSceneId(sceneDto.getId());
        sceneVersionDto.setVersion(DateUtil.format(sceneDto.getCreateTime(), DatePattern.PURE_DATETIME_PATTERN));
        sceneVersionDto.setStatus(SceneConst.SCENE_VERSION_STATUS__CONFIGURING);
        sceneVersionService.save(sceneVersionDto);

        List<ProcessExportBo> processExportBoList = sceneExportFileBo.getProcessExportBoList();
        for (ProcessExportBo processExportBo : processExportBoList) {

            // 保存流程
            VersionProcessDto versionProcessDto = new VersionProcessDto();
            versionProcessDto.setVersionId(sceneVersionDto.getId());
            versionProcessDto.setProcessId(IdUtil.randomUUID());
            versionProcessDto.setProcessName(processExportBo.getProcessName());
            versionProcessDto.setProcessXml(processExportBo.getProcessXml());
            versionProcessDto.setProcessImg(processExportBo.getProcessImg());
            versionProcessService.save(versionProcessDto);
        }

        return null;
    }

    private Long saveBytes(byte[] bytes) {
        SceneLoadPo sceneLoadPo = new SceneLoadPo();
        sceneLoadPo.setId(IdUtil.getSnowflakeNextId());
        sceneLoadPo.setZipBytes(bytes);
        sceneLoadDao.insert(sceneLoadPo);
        return sceneLoadPo.getId();
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
            // 设置config id 和 name
            configExportBo.setConfigId(configId);
            configExportBo.setConfigName(configInfo.getConfigName());
            if(configInfo != null) {
                Assert.isTrue(StrUtil.equals(action.getConnectorGroup(), configInfo.getConnectorGroup()));
                Assert.isTrue(StrUtil.equals(action.getConnectorId(), configInfo.getConnectorId()));
                Assert.isTrue(StrUtil.equals(action.getConnectorVersion(), configInfo.getConnectorVersion()));
                connectorExportBo.setConnectorGroup(configInfo.getConnectorGroup());
                connectorExportBo.setConnectorId(configInfo.getConnectorId());
                connectorExportBo.setConnectorVersion(configInfo.getConnectorVersion());
            } else {
                connectorExportBo.setConnectorGroup(action.getConnectorGroup());
                String connectorId = action.getConnectorId();
                connectorExportBo.setConnectorId(connectorId);
                connectorExportBo.setConnectorVersion(action.getConnectorVersion());
                connectorExportBo.setConnectorName(connectorId);
            }

            Map<String, ConfigExportBo> configExportBoMap = configMap.computeIfAbsent(connectorExportBo, k -> new LinkedHashMap<>());

            if(!configExportBoMap.containsKey(configExportBo.getConfigName())) {
                configExportBoMap.put(configExportBo.getConfigName(), configExportBo);
            }

        }

    }

    private static File createTempFile(String content) {
        Path path = null;
        try {
            path = Files.createTempFile("", "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        File file = path.toFile();
        FileUtil.writeUtf8String(content, file);
        return file;
    }

    private static File createTempDir(Map<String, File> srcMap, boolean deleteSrc) {
        Path path = null;
        try {
            path = Files.createTempDirectory("");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        File dir = path.toFile();

        srcMap = Opt.ofNullable(srcMap).orElse(MapUtil.<String, File>empty());
        for (Map.Entry<String, File> entry : srcMap.entrySet()) {
            String fileName = entry.getKey();
            File src = entry.getValue();
            File target = new File(dir, fileName);
            if(deleteSrc) {
                FileUtil.move(src, target, true);
            } else {
                FileUtil.copy(src, target, true);
            }
        }
        return dir;
    }

    private SceneExportFileBo parseBytes(byte[] bytes) {
        SceneExportFileBo ret = new SceneExportFileBo();

        File dir = unzip(bytes);
        File definitionFile = new File(dir, FILENAME__PROCESS_DEFINITION);
        File configFile = new File(dir, FILENAME__CONFIG);
        Assert.isTrue(fileExist(definitionFile), "process_definition.json 文件缺失");
        Assert.isTrue(fileExist(configFile), "config.json 文件缺失");
        try {
            List<ProcessExportBo> list = OM.readValue(definitionFile, PROCESS_DEFINITION_TYPE);
            ret.setProcessExportBoList(list);
        } catch (IOException e) {
            throw new RuntimeException("process_definition.json 文件格式异常", e);
        }

        try {
            List<ConnectorExportBo> list = OM.readValue(configFile, CONNECTOR_CONFIG_TYPE);
            ret.setConnectorExportBoList(list);
        } catch (IOException e) {
            throw new RuntimeException("config.json 文件格式异常", e);
        }

        return ret;
    }

    private File unzip(byte[] bytes) {
        Path path = null;
        try {
            path = Files.createTempDirectory("");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        File unzip = ZipUtil.unzip(new ByteArrayInputStream(bytes), path.toFile(), StandardCharsets.UTF_8);
        return unzip;
    }

    private boolean fileExist(File file) {
        return file.exists() && file.isFile();
    }

}
