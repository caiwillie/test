package com.brandnewdata.mop.poc.scene.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ZipUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionDto;
import com.brandnewdata.mop.poc.process.service.IProcessDefinitionService;
import com.brandnewdata.mop.poc.scene.dao.SceneProcessDao;
import com.brandnewdata.mop.poc.scene.dto.external.ProcessDefinitionExternal;
import com.brandnewdata.mop.poc.scene.dto.external.SceneProcessExternal;
import com.brandnewdata.mop.poc.scene.entity.SceneProcessEntity;
import com.brandnewdata.mop.poc.scene.request.ExportReq;
import com.dxy.library.json.jackson.JacksonUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author caiwillie
 */
@Service
public class DataExternalService {

    @Resource
    private SceneProcessDao sceneProcessDao;

    @Resource
    private IProcessDefinitionService processDefinitionService;

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
        fileMap.put("scene_process.json", createTempFile(JacksonUtil.to(sceneProcessMap)));

        List<ProcessDefinitionDto> processDefinitionDtoList = processDefinitionService.list(processIds, true);
        // 流程具体定义
        Map<String, ProcessDefinitionExternal> processDefinitionMap = new HashMap<>();
        for (ProcessDefinitionDto processDefinitionDto : processDefinitionDtoList) {
            ProcessDefinitionExternal external = new ProcessDefinitionExternal();
            String processId = processDefinitionDto.getProcessId();
            external.setId(processId);
            external.setName(processDefinitionDto.getName());
            external.setXml(processDefinitionDto.getXml());
            external.setImgUrl(processDefinitionDto.getImgUrl());
        }
        fileMap.put("process_definition.json", createTempFile(JacksonUtil.to(processDefinitionMap)));

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





}
