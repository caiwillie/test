package com.brandnewdata.mop.poc.scene.service;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.error.ErrorMessage;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionDto;
import com.brandnewdata.mop.poc.process.service.IProcessDefinitionService;
import com.brandnewdata.mop.poc.scene.dao.SceneDao;
import com.brandnewdata.mop.poc.scene.dao.SceneProcessDao;
import com.brandnewdata.mop.poc.scene.dto.SceneDto;
import com.brandnewdata.mop.poc.scene.dto.SceneProcessDto;
import com.brandnewdata.mop.poc.scene.po.ScenePo;
import com.brandnewdata.mop.poc.scene.po.SceneProcessPo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author caiwillie
 */
@Service
public class SceneService implements ISceneService {

    @Resource
    private SceneDao sceneDao;

    @Resource
    private SceneProcessDao sceneProcessDao;

    @Resource
    private IProcessDefinitionService processDefinitionService;

    @Override
    public SceneDto save(SceneDto sceneDTO) {
        Assert.notNull(sceneDTO.getName(), ErrorMessage.NOT_NULL("场景名称"));
        ScenePo entity = toEntity(sceneDTO);
        ScenePo oldEntity = exist(sceneDTO.getId());
        if(oldEntity == null) {
            sceneDao.insert(entity);
        } else {
            sceneDao.updateById(entity);
        }
        return toDTO(entity);
    }

    @Override
    public SceneProcessDto saveProcessDefinition(SceneProcessDto sceneProcessDTO) {
        // 保存完成后，得到 process id
        ProcessDefinitionDto processDefinitionDTO = processDefinitionService.save(toDTO(sceneProcessDTO));
        SceneProcessPo sceneProcessPo =
                getBusinessSceneProcessEntityByProcessId(processDefinitionDTO.getProcessId());
        if(sceneProcessPo == null) {
            // 不存在则新增
            sceneProcessPo = toEntity(sceneProcessDTO);
            // 将解析后的 process id 存入
            sceneProcessPo.setProcessId(processDefinitionDTO.getProcessId());
            sceneProcessDao.insert(sceneProcessPo);
        }

        // 更新 business scene id
        updateSceneUpdateTime(sceneProcessDTO.getBusinessSceneId());
        return toDTO(sceneProcessPo, processDefinitionDTO);
    }

    private ScenePo exist(Long id) {
        if(id == null) return null;
        return sceneDao.selectById(id);
    }

    private void updateSceneUpdateTime(Long sceneId) {
        ScenePo scenePo = sceneDao.selectById(sceneId);
        sceneDao.updateById(scenePo);
    }


    private SceneDto toDTO(ScenePo entity) {
        SceneDto dto = new SceneDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCreateTime(LocalDateTimeUtil.of(entity.getCreateTime()));
        dto.setUpdateTime(LocalDateTimeUtil.of(entity.getUpdateTime()));
        return dto;
    }

    private SceneProcessDto toDTO(SceneProcessPo sceneProcessPo,
                                  ProcessDefinitionDto processDefinitionDTO) {
        SceneProcessDto dto = new SceneProcessDto();
        dto.setId(sceneProcessPo.getId());
        dto.setBusinessSceneId(sceneProcessPo.getBusinessSceneId());
        dto.setProcessId(sceneProcessPo.getProcessId());
        if(processDefinitionDTO != null) {
            dto.setName(processDefinitionDTO.getName());
            dto.setXml(processDefinitionDTO.getXml());
            dto.setImgUrl(processDefinitionDTO.getImgUrl());
        }
        return dto;
    }

    private ScenePo toEntity(SceneDto sceneDTO) {
        ScenePo entity = new ScenePo();
        entity.setId(sceneDTO.getId());
        entity.setName(sceneDTO.getName());
        return entity;
    }

    private SceneProcessPo toEntity(SceneProcessDto sceneProcessDTO) {
        SceneProcessPo entity = new SceneProcessPo();
        entity.setId(sceneProcessDTO.getId());
        entity.setBusinessSceneId(sceneProcessDTO.getBusinessSceneId());
        return entity;
    }

    private ProcessDefinitionDto toDTO(SceneProcessDto sceneProcessDTO) {
        ProcessDefinitionDto dto = new ProcessDefinitionDto();
        dto.setProcessId(sceneProcessDTO.getProcessId());
        dto.setName(sceneProcessDTO.getName());
        dto.setXml(sceneProcessDTO.getXml());
        dto.setImgUrl(sceneProcessDTO.getImgUrl());
        return dto;
    }

    private SceneProcessPo getBusinessSceneProcessEntityByProcessId(String processId) {
        QueryWrapper<SceneProcessPo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SceneProcessPo.PROCESS_ID, processId);
        return sceneProcessDao.selectOne(queryWrapper);
    }

}
