package com.brandnewdata.mop.poc.group.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.group.dao.BusinessSceneDao;
import com.brandnewdata.mop.poc.group.dao.BusinessSceneProcessDao;
import com.brandnewdata.mop.poc.group.dto.BusinessScene;
import com.brandnewdata.mop.poc.group.entity.BusinessSceneEntity;
import com.brandnewdata.mop.poc.group.entity.BusinessSceneProcessEntity;
import com.brandnewdata.mop.poc.modeler.dto.ProcessDefinition;
import com.brandnewdata.mop.poc.modeler.service.IProcessDefinitionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
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
                BusinessScene dto = toDto(entity);
                dtos.add(dto);
            }
        }
        return new Page<>(page.getTotal(), dtos);
    }

    @Override
    public BusinessScene detail(Long id) {
        BusinessSceneEntity entity = businessSceneDao.selectById(id);
        if(entity == null) {
            return null;
        }
        BusinessScene ret = toDto(entity);

        QueryWrapper<BusinessSceneProcessEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(BusinessSceneProcessEntity.BUSINESS_SCENE_ID, id);
        List<BusinessSceneProcessEntity> sceneProcessEntities = businessSceneProcessDao.selectList(queryWrapper);

        List<ProcessDefinition> processDefinitions = new ArrayList<>();
        ret.setProcessDefinitions(processDefinitions);

        if(CollUtil.isEmpty(sceneProcessEntities)) {
            return ret;
        }

        List<String> processIds = sceneProcessEntities.stream().map(BusinessSceneProcessEntity::getProcessId).collect(Collectors.toList());

        // 确保不会产生 null
        processDefinitions.addAll(processDefinitionService.list(processIds));

        return ret;
    }


    public BusinessScene toDto(BusinessSceneEntity entity) {
        BusinessScene dto = new BusinessScene();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCreateTime(LocalDateTimeUtil.formatNormal(entity.getCreateTime()));
        dto.setUpdateTime(LocalDateTimeUtil.formatNormal(entity.getUpdateTime()));
        return dto;
    }
}
