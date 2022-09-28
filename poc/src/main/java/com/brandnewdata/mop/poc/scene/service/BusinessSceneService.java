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
import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionDTO;
import com.brandnewdata.mop.poc.process.service.IProcessDefinitionService;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService;
import com.brandnewdata.mop.poc.scene.dao.BusinessSceneDao;
import com.brandnewdata.mop.poc.scene.dao.BusinessSceneProcessDao;
import com.brandnewdata.mop.poc.scene.dto.BusinessSceneDTO;
import com.brandnewdata.mop.poc.scene.dto.BusinessSceneProcessDTO;
import com.brandnewdata.mop.poc.scene.entity.BusinessSceneEntity;
import com.brandnewdata.mop.poc.scene.entity.BusinessSceneProcessEntity;
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
public class BusinessSceneService implements IBusinessSceneService {

    @Resource
    private BusinessSceneDao businessSceneDao;

    @Resource
    private BusinessSceneProcessDao businessSceneProcessDao;

    @Resource
    private IProcessDefinitionService processDefinitionService;

    @Resource
    private IProcessDeployService processDeployService;

    @Override
    public Page<BusinessSceneDTO> page(int pageNumber, int pageSize, String name) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<BusinessSceneEntity> page =
                com.baomidou.mybatisplus.extension.plugins.pagination.Page.of(pageNumber, pageSize);
        QueryWrapper<BusinessSceneEntity> queryWrapper = new QueryWrapper<>();
        if(StrUtil.isNotBlank(name)) queryWrapper.like(BusinessSceneEntity.NAME, name); // 设置名称
        page = businessSceneDao.selectPage(page, queryWrapper);
        List<BusinessSceneEntity> entities = Optional.ofNullable(page.getRecords()).orElse(ListUtil.empty());

        List<Long> sceneIdList = entities.stream().map(BusinessSceneEntity::getId).collect(Collectors.toList());

        Collection<BusinessSceneDTO> list = list(sceneIdList, true);

        return new Page<>(page.getTotal(), ListUtil.toList(list));
    }

    @Override
    public BusinessSceneDTO getOne(Long id) {
        Collection<BusinessSceneDTO> list = list(ListUtil.of(id), true);
        return CollUtil.isEmpty(list) ? null : ListUtil.toList(list).get(0);
    }

    @Override
    public List<BusinessSceneDTO> listByIds(List<Long> ids) {
        return ListUtil.toList(list(ids, false));
    }

    @Override
    public BusinessSceneDTO save(BusinessSceneDTO businessSceneDTO) {
        Assert.notNull(businessSceneDTO.getName(), ErrorMessage.NOT_NULL("场景名称"));
        BusinessSceneEntity entity = toEntity(businessSceneDTO);
        BusinessSceneEntity oldEntity = exist(businessSceneDTO.getId());
        if(oldEntity == null) {
            businessSceneDao.insert(entity);
        } else {
            businessSceneDao.updateById(entity);
        }
        return toDTO(entity);
    }

    @Override
    public BusinessSceneProcessDTO saveProcessDefinition(BusinessSceneProcessDTO businessSceneProcessDTO) {
        // 保存完成后，得到 process id
        ProcessDefinitionDTO processDefinitionDTO = processDefinitionService.save(toDTO(businessSceneProcessDTO));
        BusinessSceneProcessEntity businessSceneProcessEntity =
                getBusinessSceneProcessEntityByProcessId(processDefinitionDTO.getProcessId());
        if(businessSceneProcessEntity == null) {
            // 不存在则新增
            businessSceneProcessEntity = toEntity(businessSceneProcessDTO);
            // 将解析后的 process id 存入
            businessSceneProcessEntity.setProcessId(processDefinitionDTO.getProcessId());
            businessSceneProcessDao.insert(businessSceneProcessEntity);
        }

        // 更新 business scene id
        updateSceneUpdateTime(businessSceneProcessDTO.getBusinessSceneId());
        return toDTO(businessSceneProcessEntity, processDefinitionDTO);
    }

    @Override
    public void deploy(BusinessSceneProcessDTO businessSceneProcessDTO) {
        ProcessDefinitionDTO processDefinitionDTO = toDTO(businessSceneProcessDTO);
        processDeployService.deploy(processDefinitionDTO, ProcessConstants.PROCESS_TYPE_SCENE);
    }

    public void deleteProcessDefinition(BusinessSceneProcessDTO businessSceneProcessDTO) {
        // 先删除流程定义
        ProcessDefinitionDTO processDefinitionDTO = toDTO(businessSceneProcessDTO);
        processDefinitionService.delete(processDefinitionDTO);

        // 再删除关联关系
        Long id = businessSceneProcessDTO.getId();
        businessSceneProcessDao.deleteById(id);
    }


    public void delete(BusinessSceneDTO businessSceneDTO) {
        // 先删除绑定的流程
        Long sceneId = businessSceneDTO.getId();
        List<BusinessSceneProcessEntity> businessSceneProcessEntities = listSceneProcessBySceneId(sceneId);
        if(CollUtil.isNotEmpty(businessSceneProcessEntities)) {
            for (BusinessSceneProcessEntity businessSceneProcessEntity : businessSceneProcessEntities) {
                BusinessSceneProcessDTO businessSceneProcessDTO = toDTO(businessSceneProcessEntity, null);
                deleteProcessDefinition(businessSceneProcessDTO);
            }
        }
        // 再删除场景
        businessSceneDao.deleteById(sceneId);
    }

    private List<BusinessSceneProcessEntity> listSceneProcessBySceneId(Long sceneId) {
        QueryWrapper<BusinessSceneProcessEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(BusinessSceneProcessEntity.BUSINESS_SCENE_ID, sceneId);
        return businessSceneProcessDao.selectList(queryWrapper);
    }

    private BusinessSceneEntity exist(Long id) {
        if(id == null) return null;
        return businessSceneDao.selectById(id);
    }

    private void updateSceneUpdateTime(Long sceneId) {
        BusinessSceneEntity sceneEntity = businessSceneDao.selectById(sceneId);
        businessSceneDao.updateById(sceneEntity);
    }

    private Collection<BusinessSceneDTO> list(List<Long> ids, boolean withXML) {
        HashMap<Long, BusinessSceneDTO> sceneMap = MapUtil.newHashMap(true);

        if(CollUtil.isEmpty(ids)) {
            return sceneMap.values();
        }
        QueryWrapper<BusinessSceneEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(BusinessSceneEntity.ID, ids);
        // 按照修改时间倒序
        queryWrapper.orderByDesc(BusinessSceneEntity.UPDATE_TIME);

        List<BusinessSceneEntity> businessSceneEntities = businessSceneDao.selectList(queryWrapper);

        if(CollUtil.isEmpty(businessSceneEntities)) {
            // sceneIds 不能为空，否则会报错
            return sceneMap.values();
        }

        List<Long> sceneIds = new ArrayList<>();
        for (BusinessSceneEntity businessSceneEntity : businessSceneEntities) {
            // 添加 sceneMap
            BusinessSceneDTO businessSceneDTO = toDTO(businessSceneEntity);
            Long id = businessSceneDTO.getId();
            sceneMap.put(id, businessSceneDTO);
            sceneIds.add(id);
        }

        QueryWrapper<BusinessSceneProcessEntity> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.in(BusinessSceneProcessEntity.BUSINESS_SCENE_ID, sceneIds);

        List<BusinessSceneProcessEntity> businessSceneProcessEntities = businessSceneProcessDao.selectList(queryWrapper2);

        if(CollUtil.isEmpty(businessSceneProcessEntities)) {
            // process ids 不能为空
            return sceneMap.values();
        }

        // 获取 process 和 scene 的映射
        Map<String, BusinessSceneProcessEntity> processSceneProcessEntityMap = businessSceneProcessEntities.stream().collect(
                Collectors.toMap(BusinessSceneProcessEntity::getProcessId, Function.identity()));

        List<ProcessDefinitionDTO> processDefinitionDTOS = processDefinitionService.list(
                ListUtil.toList(processSceneProcessEntityMap.keySet()), withXML);


        // 收集 scene 和 process definition list 的映射
        Map<Long, List<ProcessDefinitionDTO>> sceneProcessListMap = processDefinitionDTOS.stream().collect(Collectors.groupingBy(
                processDefinition -> processSceneProcessEntityMap.get(processDefinition.getProcessId()).getBusinessSceneId()));

        for (Map.Entry<Long, BusinessSceneDTO> entry : sceneMap.entrySet()) {
            Long sceneId = entry.getKey();
            BusinessSceneDTO sceneDTO = entry.getValue();

            List<ProcessDefinitionDTO> tempProcessDefinitionDTOS = sceneProcessListMap.get(sceneId);
            if(CollUtil.isEmpty(tempProcessDefinitionDTOS)) {
                continue;
            }

            // 根据最后更新时间排序
            tempProcessDefinitionDTOS = CollUtil.sort(tempProcessDefinitionDTOS, (o1, o2) -> {
                LocalDateTime time1 = Optional.ofNullable(o1.getUpdateTime()).orElse(LocalDateTime.MIN);
                LocalDateTime time2 = Optional.ofNullable(o2.getUpdateTime()).orElse(LocalDateTime.MIN);
                return time2.compareTo(time1);
            });

            ProcessDefinitionDTO first = tempProcessDefinitionDTOS.get(0);
            sceneDTO.setImgUrl(first.getImgUrl());

            List<BusinessSceneProcessDTO> sceneProcessDTOList = new ArrayList<>();
            // 如果是带XML，就需要查询出definition
            for (ProcessDefinitionDTO processDefinitionDTO : tempProcessDefinitionDTOS) {
                BusinessSceneProcessEntity businessSceneProcessEntity =
                        processSceneProcessEntityMap.get(processDefinitionDTO.getProcessId());
                BusinessSceneProcessDTO businessSceneProcessDTO = toDTO(businessSceneProcessEntity, processDefinitionDTO);
                sceneProcessDTOList.add(businessSceneProcessDTO);
            }
            sceneDTO.setProcessDefinitions(sceneProcessDTOList);
        }

        return sceneMap.values();
    }

    private BusinessSceneDTO toDTO(BusinessSceneEntity entity) {
        BusinessSceneDTO dto = new BusinessSceneDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCreateTime(LocalDateTimeUtil.of(entity.getCreateTime()));
        dto.setUpdateTime(LocalDateTimeUtil.of(entity.getUpdateTime()));
        return dto;
    }

    private BusinessSceneProcessDTO toDTO(BusinessSceneProcessEntity businessSceneProcessEntity,
                                          ProcessDefinitionDTO processDefinitionDTO) {
        BusinessSceneProcessDTO dto = new BusinessSceneProcessDTO();
        dto.setId(businessSceneProcessEntity.getId());
        dto.setBusinessSceneId(businessSceneProcessEntity.getBusinessSceneId());
        dto.setProcessId(businessSceneProcessEntity.getProcessId());
        if(processDefinitionDTO != null) {
            dto.setName(processDefinitionDTO.getName());
            dto.setXml(processDefinitionDTO.getXml());
            dto.setImgUrl(processDefinitionDTO.getImgUrl());
        }
        return dto;
    }

    private BusinessSceneEntity toEntity(BusinessSceneDTO businessSceneDTO) {
        BusinessSceneEntity entity = new BusinessSceneEntity();
        entity.setId(businessSceneDTO.getId());
        entity.setName(businessSceneDTO.getName());
        return entity;
    }

    private BusinessSceneProcessEntity toEntity(BusinessSceneProcessDTO businessSceneProcessDTO) {
        BusinessSceneProcessEntity entity = new BusinessSceneProcessEntity();
        entity.setId(businessSceneProcessDTO.getId());
        entity.setBusinessSceneId(businessSceneProcessDTO.getBusinessSceneId());
        return entity;
    }

    private ProcessDefinitionDTO toDTO(BusinessSceneProcessDTO businessSceneProcessDTO) {
        ProcessDefinitionDTO dto = new ProcessDefinitionDTO();
        dto.setProcessId(businessSceneProcessDTO.getProcessId());
        dto.setName(businessSceneProcessDTO.getName());
        dto.setXml(businessSceneProcessDTO.getXml());
        dto.setImgUrl(businessSceneProcessDTO.getImgUrl());
        return dto;
    }

    private BusinessSceneProcessEntity getBusinessSceneProcessEntityByProcessId(String processId) {
        QueryWrapper<BusinessSceneProcessEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(BusinessSceneProcessEntity.PROCESS_ID, processId);
        return businessSceneProcessDao.selectOne(queryWrapper);
    }

}
