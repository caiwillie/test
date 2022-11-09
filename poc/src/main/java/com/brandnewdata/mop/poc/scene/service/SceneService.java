package com.brandnewdata.mop.poc.scene.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.error.ErrorMessage;
import com.brandnewdata.mop.poc.process.ProcessConstants;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionDto;
import com.brandnewdata.mop.poc.process.service.IProcessDefinitionService;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService;
import com.brandnewdata.mop.poc.scene.dao.SceneDao;
import com.brandnewdata.mop.poc.scene.dao.SceneProcessDao;
import com.brandnewdata.mop.poc.scene.dto.SceneDTO;
import com.brandnewdata.mop.poc.scene.dto.SceneProcessDTO;
import com.brandnewdata.mop.poc.scene.entity.SceneEntity;
import com.brandnewdata.mop.poc.scene.entity.SceneProcessEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Resource
    private IProcessDeployService processDeployService;

    @Override
    public Page<SceneDTO> page(int pageNumber, int pageSize, String name) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SceneEntity> page =
                com.baomidou.mybatisplus.extension.plugins.pagination.Page.of(pageNumber, pageSize);
        QueryWrapper<SceneEntity> queryWrapper = new QueryWrapper<>();
        if(StrUtil.isNotBlank(name)) queryWrapper.like(SceneEntity.NAME, name); // 设置名称
        page = sceneDao.selectPage(page, queryWrapper);
        List<SceneEntity> entities = Optional.ofNullable(page.getRecords()).orElse(ListUtil.empty());

        List<Long> sceneIdList = entities.stream().map(SceneEntity::getId).collect(Collectors.toList());

        Collection<SceneDTO> list = list(sceneIdList, true);

        return new Page<>(page.getTotal(), ListUtil.toList(list));
    }

    @Override
    public SceneDTO getOne(Long id) {
        Collection<SceneDTO> list = list(ListUtil.of(id), true);
        return CollUtil.isEmpty(list) ? null : ListUtil.toList(list).get(0);
    }

    @Override
    public List<SceneDTO> listByIds(List<Long> ids) {
        return ListUtil.toList(list(ids, false));
    }

    @Override
    public SceneDTO save(SceneDTO sceneDTO) {
        Assert.notNull(sceneDTO.getName(), ErrorMessage.NOT_NULL("场景名称"));
        SceneEntity entity = toEntity(sceneDTO);
        SceneEntity oldEntity = exist(sceneDTO.getId());
        if(oldEntity == null) {
            sceneDao.insert(entity);
        } else {
            sceneDao.updateById(entity);
        }
        return toDTO(entity);
    }

    @Override
    public SceneProcessDTO saveProcessDefinition(SceneProcessDTO sceneProcessDTO) {
        // 保存完成后，得到 process id
        ProcessDefinitionDto processDefinitionDTO = processDefinitionService.save(toDTO(sceneProcessDTO));
        SceneProcessEntity sceneProcessEntity =
                getBusinessSceneProcessEntityByProcessId(processDefinitionDTO.getProcessId());
        if(sceneProcessEntity == null) {
            // 不存在则新增
            sceneProcessEntity = toEntity(sceneProcessDTO);
            // 将解析后的 process id 存入
            sceneProcessEntity.setProcessId(processDefinitionDTO.getProcessId());
            sceneProcessDao.insert(sceneProcessEntity);
        }

        // 更新 business scene id
        updateSceneUpdateTime(sceneProcessDTO.getBusinessSceneId());
        return toDTO(sceneProcessEntity, processDefinitionDTO);
    }

    @Override
    public void deploy(SceneProcessDTO sceneProcessDTO) {
        ProcessDefinitionDto processDefinitionDTO = toDTO(sceneProcessDTO);
        processDeployService.deploy(processDefinitionDTO, ProcessConstants.PROCESS_TYPE_SCENE);
    }

    @Override
    public void deleteProcessDefinition(SceneProcessDTO sceneProcessDTO) {
        // 先删除流程定义
        ProcessDefinitionDto processDefinitionDTO = toDTO(sceneProcessDTO);
        processDefinitionService.delete(processDefinitionDTO);

        // 再删除关联关系
        Long id = sceneProcessDTO.getId();
        sceneProcessDao.deleteById(id);
    }

    @Override
    public void delete(SceneDTO sceneDTO) {
        // 先删除绑定的流程
        Long sceneId = sceneDTO.getId();
        List<SceneProcessEntity> businessSceneProcessEntities = listSceneProcessBySceneId(sceneId);
        if(CollUtil.isNotEmpty(businessSceneProcessEntities)) {
            for (SceneProcessEntity sceneProcessEntity : businessSceneProcessEntities) {
                SceneProcessDTO sceneProcessDTO = toDTO(sceneProcessEntity, null);
                deleteProcessDefinition(sceneProcessDTO);
            }
        }
        // 再删除场景
        sceneDao.deleteById(sceneId);
    }

    private List<SceneProcessEntity> listSceneProcessBySceneId(Long sceneId) {
        QueryWrapper<SceneProcessEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SceneProcessEntity.BUSINESS_SCENE_ID, sceneId);
        return sceneProcessDao.selectList(queryWrapper);
    }

    private SceneEntity exist(Long id) {
        if(id == null) return null;
        return sceneDao.selectById(id);
    }

    private void updateSceneUpdateTime(Long sceneId) {
        SceneEntity sceneEntity = sceneDao.selectById(sceneId);
        sceneDao.updateById(sceneEntity);
    }

    private Collection<SceneDTO> list(List<Long> ids, boolean withXML) {
        HashMap<Long, SceneDTO> sceneMap = MapUtil.newHashMap(true);

        if(CollUtil.isEmpty(ids)) {
            return sceneMap.values();
        }
        QueryWrapper<SceneEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(SceneEntity.ID, ids);
        // 按照修改时间倒序
        queryWrapper.orderByDesc(SceneEntity.UPDATE_TIME);

        List<SceneEntity> businessSceneEntities = sceneDao.selectList(queryWrapper);

        if(CollUtil.isEmpty(businessSceneEntities)) {
            // sceneIds 不能为空，否则会报错
            return sceneMap.values();
        }

        List<Long> sceneIds = new ArrayList<>();
        for (SceneEntity sceneEntity : businessSceneEntities) {
            // 添加 sceneMap
            SceneDTO sceneDTO = toDTO(sceneEntity);
            Long id = sceneDTO.getId();
            sceneMap.put(id, sceneDTO);
            sceneIds.add(id);
        }

        QueryWrapper<SceneProcessEntity> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.in(SceneProcessEntity.BUSINESS_SCENE_ID, sceneIds);

        List<SceneProcessEntity> businessSceneProcessEntities = sceneProcessDao.selectList(queryWrapper2);

        if(CollUtil.isEmpty(businessSceneProcessEntities)) {
            // process ids 不能为空
            return sceneMap.values();
        }

        // 获取 process 和 scene 的映射
        Map<String, SceneProcessEntity> processSceneProcessEntityMap = businessSceneProcessEntities.stream().collect(
                Collectors.toMap(SceneProcessEntity::getProcessId, Function.identity()));

        List<ProcessDefinitionDto> processDefinitionDtos = processDefinitionService.list(
                ListUtil.toList(processSceneProcessEntityMap.keySet()), withXML);


        // 收集 scene 和 process definition list 的映射
        Map<Long, List<ProcessDefinitionDto>> sceneProcessListMap = processDefinitionDtos.stream().collect(Collectors.groupingBy(
                processDefinition -> processSceneProcessEntityMap.get(processDefinition.getProcessId()).getBusinessSceneId()));

        for (Map.Entry<Long, SceneDTO> entry : sceneMap.entrySet()) {
            Long sceneId = entry.getKey();
            SceneDTO sceneDTO = entry.getValue();

            List<ProcessDefinitionDto> tempProcessDefinitionDtos = sceneProcessListMap.get(sceneId);
            if(CollUtil.isEmpty(tempProcessDefinitionDtos)) {
                continue;
            }

            // 根据最后更新时间排序
            tempProcessDefinitionDtos = CollUtil.sort(tempProcessDefinitionDtos, (o1, o2) -> {
                LocalDateTime time1 = Optional.ofNullable(o1.getUpdateTime()).orElse(LocalDateTime.MIN);
                LocalDateTime time2 = Optional.ofNullable(o2.getUpdateTime()).orElse(LocalDateTime.MIN);
                return time2.compareTo(time1);
            });

            ProcessDefinitionDto first = tempProcessDefinitionDtos.get(0);
            sceneDTO.setImgUrl(first.getImgUrl());

            List<SceneProcessDTO> sceneProcessDTOList = new ArrayList<>();
            // 如果是带XML，就需要查询出definition
            for (ProcessDefinitionDto processDefinitionDTO : tempProcessDefinitionDtos) {
                SceneProcessEntity sceneProcessEntity =
                        processSceneProcessEntityMap.get(processDefinitionDTO.getProcessId());
                SceneProcessDTO sceneProcessDTO = toDTO(sceneProcessEntity, processDefinitionDTO);
                sceneProcessDTOList.add(sceneProcessDTO);
            }
            sceneDTO.setProcessDefinitions(sceneProcessDTOList);
        }

        return sceneMap.values();
    }

    private SceneDTO toDTO(SceneEntity entity) {
        SceneDTO dto = new SceneDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCreateTime(LocalDateTimeUtil.of(entity.getCreateTime()));
        dto.setUpdateTime(LocalDateTimeUtil.of(entity.getUpdateTime()));
        return dto;
    }

    private SceneProcessDTO toDTO(SceneProcessEntity sceneProcessEntity,
                                  ProcessDefinitionDto processDefinitionDTO) {
        SceneProcessDTO dto = new SceneProcessDTO();
        dto.setId(sceneProcessEntity.getId());
        dto.setBusinessSceneId(sceneProcessEntity.getBusinessSceneId());
        dto.setProcessId(sceneProcessEntity.getProcessId());
        if(processDefinitionDTO != null) {
            dto.setName(processDefinitionDTO.getName());
            dto.setXml(processDefinitionDTO.getXml());
            dto.setImgUrl(processDefinitionDTO.getImgUrl());
        }
        return dto;
    }

    private SceneEntity toEntity(SceneDTO sceneDTO) {
        SceneEntity entity = new SceneEntity();
        entity.setId(sceneDTO.getId());
        entity.setName(sceneDTO.getName());
        return entity;
    }

    private SceneProcessEntity toEntity(SceneProcessDTO sceneProcessDTO) {
        SceneProcessEntity entity = new SceneProcessEntity();
        entity.setId(sceneProcessDTO.getId());
        entity.setBusinessSceneId(sceneProcessDTO.getBusinessSceneId());
        return entity;
    }

    private ProcessDefinitionDto toDTO(SceneProcessDTO sceneProcessDTO) {
        ProcessDefinitionDto dto = new ProcessDefinitionDto();
        dto.setProcessId(sceneProcessDTO.getProcessId());
        dto.setName(sceneProcessDTO.getName());
        dto.setXml(sceneProcessDTO.getXml());
        dto.setImgUrl(sceneProcessDTO.getImgUrl());
        return dto;
    }

    private SceneProcessEntity getBusinessSceneProcessEntityByProcessId(String processId) {
        QueryWrapper<SceneProcessEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SceneProcessEntity.PROCESS_ID, processId);
        return sceneProcessDao.selectOne(queryWrapper);
    }

}
