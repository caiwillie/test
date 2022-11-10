package com.brandnewdata.mop.poc.scene.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ZipUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.manager.ConnectorManager;
import com.brandnewdata.mop.poc.manager.dto.ConfigInfo;
import com.brandnewdata.mop.poc.manager.dto.ConnectorBasicInfo;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionDto;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParseStep1;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParser;
import com.brandnewdata.mop.poc.process.parser.dto.Action;
import com.brandnewdata.mop.poc.process.service.IProcessDefinitionService;
import com.brandnewdata.mop.poc.process.util.ProcessUtil;
import com.brandnewdata.mop.poc.scene.dao.SceneLoadDao;
import com.brandnewdata.mop.poc.scene.dao.SceneProcessDao;
import com.brandnewdata.mop.poc.scene.dto.external.ConfigExternal;
import com.brandnewdata.mop.poc.scene.dto.external.ProcessDefinitionExternal;
import com.brandnewdata.mop.poc.scene.dto.external.SceneProcessExternal;
import com.brandnewdata.mop.poc.scene.entity.SceneProcessEntity;
import com.brandnewdata.mop.poc.scene.request.ExportReq;
import com.brandnewdata.mop.poc.scene.response.ConnConfResp;
import com.brandnewdata.mop.poc.scene.response.LoadResp;
import com.dxy.library.json.jackson.JacksonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author caiwillie
 */
@Service
public class DataExternalService {

    private static final String FILENAME__SCENE_PROCESS = "scene_process.json";
    private static final String FILENAME__PROCESS_DEFINITION = "process_definition.json";

    private static final String FILENAME__CONFIG = "config.json";

    private static final ObjectMapper OM = JacksonUtil.getObjectMapper();

    private static final MapType MAP_TYPE1 =
            OM.getTypeFactory().constructMapType(Map.class, Long.class, SceneProcessExternal.class);

    private static final MapType MAP_TYPE2 =
            OM.getTypeFactory().constructMapType(Map.class, String.class, ProcessDefinitionExternal.class);

    private static final MapType MAP_TYPE3 =
            OM.getTypeFactory().constructMapType(Map.class, String.class, ConfigExternal.class);

    @Resource
    private SceneProcessDao sceneProcessDao;

    @Resource
    private IProcessDefinitionService processDefinitionService;

    @Resource
    private SceneLoadDao loadDao;

    @Resource
    private ConnectorManager connectorManager;

    public File export(ExportReq req) {
        Assert.isTrue(CollUtil.isNotEmpty(req.getProcessIds()), "所选流程不能为空");
        QueryWrapper<SceneProcessEntity> queryWrapper = new QueryWrapper<>();
        List<SceneProcessEntity> entities = sceneProcessDao.selectList(queryWrapper);

        Map<String, File> fileMap = new HashMap<>();

        // 场景下的流程
        Map<Long, SceneProcessExternal> sceneProcessMap = new HashMap<>();
        List<String> processIds = new ArrayList<>();
        for (SceneProcessEntity entity : entities) {
            SceneProcessExternal external = new SceneProcessExternal();
            Long id = entity.getId();
            String processId = entity.getProcessId();
            external.setId(id);
            external.setSceneId(entity.getBusinessSceneId());
            external.setProcessId(processId);
            sceneProcessMap.put(id, external);
            processIds.add(processId);
        }
        // 获取file
        fileMap.put(FILENAME__SCENE_PROCESS, createTempFile(JacksonUtil.to(sceneProcessMap)));

        List<ProcessDefinitionDto> processDefinitionDtoList = processDefinitionService.list(processIds, true);
        // 流程具体定义
        Map<String, ProcessDefinitionExternal> processDefinitionMap = new HashMap<>();
        Map<String, String> configs = new HashMap<>();
        for (ProcessDefinitionDto processDefinitionDto : processDefinitionDtoList) {
            ProcessDefinitionExternal external = new ProcessDefinitionExternal();
            String processId = processDefinitionDto.getProcessId();
            String name = processDefinitionDto.getName();
            String xml = processDefinitionDto.getXml();
            external.setId(processId);
            external.setName(name);
            external.setXml(xml);
            external.setImgUrl(processDefinitionDto.getImgUrl());
            // 缓存连接器
            processDefinitionMap.put(processId, external);

            // 解析流程中用到的流程
            ProcessDefinitionParseStep1 step1 = ProcessDefinitionParser.step1(processId, name, xml);
            configs.putAll(Opt.ofNullable(step1.parseConfig().step1Result().getConfigs()).orElse(MapUtil.empty()));
        }
        fileMap.put(FILENAME__PROCESS_DEFINITION, createTempFile(JacksonUtil.to(processDefinitionMap)));

        Map<String, ConfigExternal> configExternalMap = getConfigExternalMap(configs);
        fileMap.put(FILENAME__CONFIG, createTempFile(JacksonUtil.to(configExternalMap)));

        File dir = createTempDir(fileMap, true);
        File zip = ZipUtil.zip(dir);
        return zip;
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

    private Map<String, ConfigExternal> getConfigExternalMap(Map<String, String> configs) {
        Map<String, ConfigExternal> ret = new HashMap<>();
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            String configId = entry.getKey();
            String actionFullId = entry.getValue();
            // 根据 fullId 解析获取连接器信息
            Action action = ProcessUtil.parseActionInfo(actionFullId);
            ConnectorBasicInfo connectorBasicInfo = connectorManager.getConnectorBasicInfo(action.getConnectorGroup(),
                    action.getConnectorId(), action.getConnectorVersion());
            Assert.notNull(connectorBasicInfo, "[0x01] 连接器不存在");

            ConfigExternal configExternal = new ConfigExternal();
            // 连接器相关
            configExternal.setConnectorGroup(connectorBasicInfo.getConnectorGroup());
            configExternal.setConnectorId(connectorBasicInfo.getConnectorId());
            configExternal.setConnectorVersion(connectorBasicInfo.getConnectorVersion());
            configExternal.setConnectorName(connectorBasicInfo.getConnectorName());
            configExternal.setConnectorIcon(connectorBasicInfo.getConnectorSmallIcon());

            configExternal.setConfigId(String.valueOf(configId));
            ConfigInfo configInfo = connectorManager.getConfigInfo(configId);
            if(configInfo != null) {
                configExternal.setConfigName(configInfo.getConfigName());
            }
        }
        return ret;
    }

    public LoadResp load(byte[] bytes) {
        LoadResp resp = new LoadResp();
        File dir = unzip(bytes);

        File sceneFile = new File(dir, FILENAME__SCENE_PROCESS);
        File definitionFile = new File(dir, FILENAME__PROCESS_DEFINITION);
        File configFile = new File(dir, FILENAME__CONFIG);
        Assert.isTrue(fileExist(sceneFile), "[0x01] 文件缺失");
        Assert.isTrue(fileExist(definitionFile), "[0x02] 文件缺失");
        Assert.isTrue(fileExist(configFile), "[0x03] 文件缺失");

        // 解析压缩包内的文件
        Map<Long, SceneProcessExternal> processMap = null;
        Map<String, ProcessDefinitionExternal> definitionMap = null;
        Map<String, ConfigExternal> configMap = null;
        try {
            processMap = OM.readValue(sceneFile, MAP_TYPE1);
            processMap = Opt.ofNullable(processMap).orElse(MapUtil.empty());
            definitionMap = OM.readValue(definitionFile, MAP_TYPE2);
            definitionMap = Opt.ofNullable(definitionMap).orElse(MapUtil.empty());
            configMap = OM.readValue(configFile, MAP_TYPE3);
            configMap = Opt.ofNullable(configMap).orElse(MapUtil.empty());
        } catch (IOException e) {
            throw new IllegalArgumentException("[0x01] 文件格式错误");
        }

        resp.setId(1L);
        List<ConnConfResp> configureList = getConnConfRespList(configMap.values());
        resp.setConfigureList(configureList);

        return resp;
    }

    private List<ConnConfResp> getConnConfRespList(Collection<ConfigExternal> configExternals) {
        List<ConnConfResp> ret = new ArrayList<>();
        for (ConfigExternal configExternal : configExternals) {
            ConnConfResp connConfResp = new ConnConfResp();
            connConfResp.setConnectorGroup(configExternal.getConnectorGroup());
            connConfResp.setConnectorId(configExternal.getConnectorId());
            connConfResp.setConnectorName(configExternal.getConnectorName());
            connConfResp.setConnectorVersion(configExternal.getConnectorVersion());
            connConfResp.setConnectorIcon(configExternal.getConnectorIcon());
            connConfResp.setConfigureId(configExternal.getConfigId());
            connConfResp.setConfigureName(configExternal.getConfigName());
            ret.add(connConfResp);
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
