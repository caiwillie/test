package com.brandnewdata.mop.poc.group.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.error.ErrorMessage;
import com.brandnewdata.mop.poc.group.dao.BusinessSceneDao;
import com.brandnewdata.mop.poc.group.dao.BusinessSceneProcessDao;
import com.brandnewdata.mop.poc.group.dto.BusinessScene;
import com.brandnewdata.mop.poc.group.dto.BusinessSceneProcessDefinition;
import com.brandnewdata.mop.poc.group.entity.BusinessSceneEntity;
import com.brandnewdata.mop.poc.group.entity.BusinessSceneProcessEntity;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinition;
import com.brandnewdata.mop.poc.process.service.IProcessDefinitionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Override
    public Page<BusinessScene> page(int pageNumber, int pageSize) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<BusinessSceneEntity> page =
                com.baomidou.mybatisplus.extension.plugins.pagination.Page.of(pageNumber, pageSize);
        QueryWrapper<BusinessSceneEntity> queryWrapper = new QueryWrapper<>();
        page = businessSceneDao.selectPage(page, queryWrapper);
        List<BusinessSceneEntity> entities = page.getRecords();
        List<BusinessScene> dtos = new ArrayList<>();
        if(CollUtil.isNotEmpty(entities)) {
            for (BusinessSceneEntity entity : entities) {
                BusinessScene dto = toDTO(entity);
                dtos.add(dto);
            }
        }
        return new Page<>(page.getTotal(), dtos);
    }

    @Override
    public BusinessScene getOne(Long id) {
        Assert.notNull(id, ErrorMessage.NOT_NULL("场景 id"));
        BusinessSceneEntity entity = businessSceneDao.selectById(id);
        if(entity == null) {
            return null;
        }
        BusinessScene ret = toDTO(entity);

        QueryWrapper<BusinessSceneProcessEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(BusinessSceneProcessEntity.BUSINESS_SCENE_ID, id);
        List<BusinessSceneProcessEntity> businessSceneProcessEntities = businessSceneProcessDao.selectList(queryWrapper);

        // 场景流程关联
        List<BusinessSceneProcessDefinition> businessSceneProcessDefinitions = new ArrayList<>();
        ret.setProcessDefinitions(businessSceneProcessDefinitions);

        if(CollUtil.isEmpty(businessSceneProcessEntities)) {
            return ret;
        }

        List<String> processIds = businessSceneProcessEntities.stream().map(BusinessSceneProcessEntity::getProcessId).collect(Collectors.toList());

        List<ProcessDefinition> processDefinitions = processDefinitionService.list(processIds);

        // get 流程定义 map
        Map<String, ProcessDefinition> processDefinitionMap = processDefinitions.stream()
                .collect(Collectors.toMap(ProcessDefinition::getProcessId, Function.identity()));

        for (BusinessSceneProcessEntity businessSceneProcessEntity : businessSceneProcessEntities) {
            String processId = businessSceneProcessEntity.getProcessId();
            ProcessDefinition processDefinition = processDefinitionMap.get(processId);
            Assert.notNull(processDefinition, ErrorMessage.STALE_DATA_NOT_EXIST("流程定义", processId));
            BusinessSceneProcessDefinition businessSceneProcessDefinition = toDTO(businessSceneProcessEntity, processDefinition);
            businessSceneProcessDefinitions.add(businessSceneProcessDefinition);
        }

        return ret;
    }

    @Override
    public BusinessScene save(BusinessScene businessScene) {
        Assert.notNull(businessScene.getName(), ErrorMessage.NOT_NULL("场景名称"));
        BusinessSceneEntity entity = toEntity(businessScene);
        Long id = entity.getId();
        if(id == null) {
            businessSceneDao.insert(entity);
        } else {
            businessSceneDao.updateById(entity);
        }
        return toDTO(entity);
    }

    @Override
    public BusinessSceneProcessDefinition saveProcessDefinition(BusinessSceneProcessDefinition businessSceneProcessDefinition) {
        // 保存完成后，得到 process id
        ProcessDefinition processDefinition = processDefinitionService.save(toDTO(businessSceneProcessDefinition));
        BusinessSceneProcessEntity businessSceneProcessEntity =
                getBusinessSceneProcessEntityByProcessId(processDefinition.getProcessId());
        if(businessSceneProcessEntity == null) {
            // 不存在则新增
            businessSceneProcessEntity = toEntity(businessSceneProcessDefinition);
            // 将解析后的 process id 存入
            businessSceneProcessEntity.setProcessId(processDefinition.getProcessId());
            businessSceneProcessDao.insert(businessSceneProcessEntity);
        }
        return toDTO(businessSceneProcessEntity, processDefinition);
    }

    private BusinessSceneProcessEntity getBusinessSceneProcessEntityByProcessId(String processId) {
        QueryWrapper<BusinessSceneProcessEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(BusinessSceneProcessEntity.PROCESS_ID, processId);
        return businessSceneProcessDao.selectOne(queryWrapper);
    }

    public BusinessScene toDTO(BusinessSceneEntity entity) {
        BusinessScene dto = new BusinessScene();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCreateTime(LocalDateTimeUtil.formatNormal(entity.getCreateTime()));
        dto.setUpdateTime(LocalDateTimeUtil.formatNormal(entity.getUpdateTime()));
        return dto;
    }

    public BusinessSceneProcessDefinition toDTO(BusinessSceneProcessEntity businessSceneProcessEntity,
                                                ProcessDefinition processDefinition) {
        BusinessSceneProcessDefinition dto = new BusinessSceneProcessDefinition();
        dto.setId(businessSceneProcessEntity.getId());
        dto.setBusinessSceneId(businessSceneProcessEntity.getBusinessSceneId());
        dto.setProcessId(businessSceneProcessEntity.getProcessId());
        dto.setName(processDefinition.getName());
        dto.setXml(processDefinition.getXml());
        return dto;
    }

    public BusinessSceneEntity toEntity(BusinessScene businessScene) {
        BusinessSceneEntity entity = new BusinessSceneEntity();
        entity.setId(businessScene.getId());
        entity.setName(businessScene.getName());
        return entity;
    }

    public BusinessSceneProcessEntity toEntity(BusinessSceneProcessDefinition businessSceneProcessDefinition) {
        BusinessSceneProcessEntity entity = new BusinessSceneProcessEntity();
        entity.setId(businessSceneProcessDefinition.getId());
        entity.setBusinessSceneId(businessSceneProcessDefinition.getBusinessSceneId());
        return entity;
    }

    public ProcessDefinition toDTO(BusinessSceneProcessDefinition businessSceneProcessDefinition) {
        ProcessDefinition dto = new ProcessDefinition();
        dto.setProcessId(businessSceneProcessDefinition.getProcessId());
        dto.setName(businessSceneProcessDefinition.getName());
        dto.setXml(businessSceneProcessDefinition.getXml());
        return dto;
    }


}
