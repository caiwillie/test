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
import com.brandnewdata.mop.poc.scene.dao.BusinessSceneDao;
import com.brandnewdata.mop.poc.scene.dao.BusinessSceneProcessDao;
import com.brandnewdata.mop.poc.scene.dto.BusinessSceneDTO;
import com.brandnewdata.mop.poc.scene.dto.BusinessSceneProcessDTO;
import com.brandnewdata.mop.poc.scene.entity.BusinessSceneEntity;
import com.brandnewdata.mop.poc.scene.entity.BusinessSceneProcessEntity;
import com.brandnewdata.mop.poc.process.ProcessConstants;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinition;
import com.brandnewdata.mop.poc.process.service.IProcessDefinitionService;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService;
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
        List<BusinessSceneDTO> dtos = new ArrayList<>();
        if(CollUtil.isNotEmpty(entities)) {
            for (BusinessSceneEntity entity : entities) {
                BusinessSceneDTO dto = getOne(entity.getId());
                dtos.add(dto);
            }
        }
        return new Page<>(page.getTotal(), dtos);
    }

    @Override
    public BusinessSceneDTO getOne(Long id) {
        Assert.notNull(id, ErrorMessage.NOT_NULL("场景 id"));
        BusinessSceneEntity entity = businessSceneDao.selectById(id);
        if(entity == null) {
            return null;
        }
        BusinessSceneDTO ret = toDTO(entity);

        QueryWrapper<BusinessSceneProcessEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(BusinessSceneProcessEntity.BUSINESS_SCENE_ID, id);
        List<BusinessSceneProcessEntity> businessSceneProcessEntities = businessSceneProcessDao.selectList(queryWrapper);

        // 场景流程关联
        List<BusinessSceneProcessDTO> businessSceneProcessDTOS = new ArrayList<>();
        ret.setProcessDefinitions(businessSceneProcessDTOS);

        if(CollUtil.isEmpty(businessSceneProcessEntities)) {
            return ret;
        }

        // 获取场景关联的processId
        List<String> processIds = businessSceneProcessEntities.stream().map(BusinessSceneProcessEntity::getProcessId).collect(Collectors.toList());

        List<ProcessDefinition> processDefinitions = processDefinitionService.list(processIds, false);

        // 比较流程列表的更新时间
        Optional<ProcessDefinition> first = processDefinitions.stream().min((o1, o2) -> {
            LocalDateTime time1 = Optional.ofNullable(o1.getUpdateTime()).orElse(LocalDateTime.MIN);
            LocalDateTime time2 = Optional.ofNullable(o2.getUpdateTime()).orElse(LocalDateTime.MIN);
            return time2.compareTo(time1);
        });

        // 取最后更新的流程中的 img 作为流程图
        first.ifPresent(processDefinition -> ret.setImgUrl(processDefinition.getImgUrl()));

        // get 流程定义 map
        Map<String, ProcessDefinition> processDefinitionMap = processDefinitions.stream()
                .collect(Collectors.toMap(ProcessDefinition::getProcessId, Function.identity()));

        for (BusinessSceneProcessEntity businessSceneProcessEntity : businessSceneProcessEntities) {
            String processId = businessSceneProcessEntity.getProcessId();
            ProcessDefinition processDefinition = processDefinitionMap.get(processId);
            Assert.notNull(processDefinition, ErrorMessage.STALE_DATA_NOT_EXIST("流程定义", processId));
            BusinessSceneProcessDTO businessSceneProcessDTO = toDTO(businessSceneProcessEntity, processDefinition);
            businessSceneProcessDTOS.add(businessSceneProcessDTO);
        }

        return ret;
    }

    @Override
    public List<BusinessSceneDTO> listByIds(List<Long> ids) {
        List<BusinessSceneDTO> ret = new ArrayList<>();
        if(CollUtil.isEmpty(ids)) {
            return ret;
        }



        return null;
    }

    @Override
    public BusinessSceneDTO save(BusinessSceneDTO businessSceneDTO) {
        Assert.notNull(businessSceneDTO.getName(), ErrorMessage.NOT_NULL("场景名称"));
        BusinessSceneEntity entity = toEntity(businessSceneDTO);
        Long id = entity.getId();
        if(id == null) {
            businessSceneDao.insert(entity);
        } else {
            businessSceneDao.updateById(entity);
        }
        return toDTO(entity);
    }

    @Override
    public BusinessSceneProcessDTO saveProcessDefinition(BusinessSceneProcessDTO businessSceneProcessDTO) {
        // 保存完成后，得到 process id
        ProcessDefinition processDefinition = processDefinitionService.save(toDTO(businessSceneProcessDTO));
        BusinessSceneProcessEntity businessSceneProcessEntity =
                getBusinessSceneProcessEntityByProcessId(processDefinition.getProcessId());
        if(businessSceneProcessEntity == null) {
            // 不存在则新增
            businessSceneProcessEntity = toEntity(businessSceneProcessDTO);
            // 将解析后的 process id 存入
            businessSceneProcessEntity.setProcessId(processDefinition.getProcessId());
            businessSceneProcessDao.insert(businessSceneProcessEntity);
        }
        return toDTO(businessSceneProcessEntity, processDefinition);
    }

    @Override
    public void deploy(BusinessSceneProcessDTO businessSceneProcessDTO) {
        ProcessDefinition processDefinition = toDTO(businessSceneProcessDTO);
        processDeployService.deploy(processDefinition, ProcessConstants.PROCESS_TYPE_SCENE);
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

        List<ProcessDefinition> processDefinitions = processDefinitionService.list(
                ListUtil.toList(processSceneProcessEntityMap.keySet()), withXML);


        // 收集 scene 和 process definition list 的映射
        Map<Long, List<ProcessDefinition>> sceneProcessListMap = processDefinitions.stream().collect(Collectors.groupingBy(
                processDefinition -> processSceneProcessEntityMap.get(processDefinition.getProcessId()).getBusinessSceneId()));

        for (Map.Entry<Long, BusinessSceneDTO> entry : sceneMap.entrySet()) {
            Long sceneId = entry.getKey();
            BusinessSceneDTO sceneDTO = entry.getValue();

            List<ProcessDefinition> tempProcessDefinitions = sceneProcessListMap.get(sceneId);
            if(CollUtil.isEmpty(tempProcessDefinitions)) {
                continue;
            }

            // 根据最后更新时间排序
            tempProcessDefinitions = CollUtil.sort(tempProcessDefinitions, (o1, o2) -> {
                LocalDateTime time1 = Optional.ofNullable(o1.getUpdateTime()).orElse(LocalDateTime.MIN);
                LocalDateTime time2 = Optional.ofNullable(o2.getUpdateTime()).orElse(LocalDateTime.MIN);
                return time2.compareTo(time1);
            });

            ProcessDefinition first = tempProcessDefinitions.get(0);
            sceneDTO.setImgUrl(first.getImgUrl());

            if(withXML) {
                List<BusinessSceneProcessDTO> sceneProcessDTOList = new ArrayList<>();
                // 如果是带XML，就需要查询出definition
                for (ProcessDefinition processDefinition : tempProcessDefinitions) {
                    BusinessSceneProcessEntity businessSceneProcessEntity =
                            processSceneProcessEntityMap.get(processDefinition.getProcessId());
                    BusinessSceneProcessDTO businessSceneProcessDTO = toDTO(businessSceneProcessEntity, processDefinition);
                    sceneProcessDTOList.add(businessSceneProcessDTO);
                }
                sceneDTO.setProcessDefinitions(sceneProcessDTOList);
            }

        }

        return sceneMap.values();
    }

    private BusinessSceneDTO toDTO(BusinessSceneEntity entity) {
        BusinessSceneDTO dto = new BusinessSceneDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCreateTime(LocalDateTimeUtil.formatNormal(entity.getCreateTime()));
        dto.setUpdateTime(LocalDateTimeUtil.formatNormal(entity.getUpdateTime()));
        return dto;
    }

    private BusinessSceneProcessDTO toDTO(BusinessSceneProcessEntity businessSceneProcessEntity,
                                          ProcessDefinition processDefinition) {
        BusinessSceneProcessDTO dto = new BusinessSceneProcessDTO();
        dto.setId(businessSceneProcessEntity.getId());
        dto.setBusinessSceneId(businessSceneProcessEntity.getBusinessSceneId());
        dto.setProcessId(businessSceneProcessEntity.getProcessId());
        dto.setName(processDefinition.getName());
        dto.setXml(processDefinition.getXml());
        dto.setImgUrl(processDefinition.getImgUrl());
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

    private ProcessDefinition toDTO(BusinessSceneProcessDTO businessSceneProcessDTO) {
        ProcessDefinition dto = new ProcessDefinition();
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
