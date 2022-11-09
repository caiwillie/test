package com.brandnewdata.mop.poc.process.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.error.ErrorMessage;
import com.brandnewdata.mop.poc.process.dao.ProcessDefinitionDao;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionDto;
import com.brandnewdata.mop.poc.process.entity.ProcessDefinitionEntity;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParser;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProcessDefinitionServiceImpl implements IProcessDefinitionService {

    @Resource
    private ProcessDefinitionDao processDefinitionDao;
    
    @Override
    public List<ProcessDefinitionDto> list(List<String> ids, boolean withXML) {
        List<ProcessDefinitionDto> ret = new ArrayList<>();

        if(CollUtil.isEmpty(ids)) {
            return ret;
        }

        QueryWrapper<ProcessDefinitionEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(ProcessDefinitionEntity.ID, ids);
        if(!withXML) {
            // 不需要 xml 的话，就不需要查询xml字段
            queryWrapper.select(ProcessDefinitionEntity.class,
                    tableFieldInfo -> !StrUtil.equals(tableFieldInfo.getColumn(), ProcessDefinitionEntity.XML));
        }

        List<ProcessDefinitionEntity> entities = processDefinitionDao.selectList(queryWrapper);
        if(CollUtil.isNotEmpty(entities)) {
            for (ProcessDefinitionEntity entity : entities) {
                ProcessDefinitionDto dto = toDTO(entity);
                ret.add(dto);
            }
        }

        return ret;
    }

    @Override
    public ProcessDefinitionDto save(ProcessDefinitionDto processDefinitionDTO) {


        // dto to entity 逻辑特殊，不提取公共
        ProcessDefinitionEntity entity = new ProcessDefinitionEntity();
        entity.setImgUrl(processDefinitionDTO.getImgUrl());
        entity.setXml(processDefinitionDTO.getXml());

        // 返回的 processDefinitionDTO 发生了一些改变，一些数据得到补充
        processDefinitionDTO = ProcessDefinitionParser.newInstance(processDefinitionDTO).buildProcessDefinition();
        String processId = processDefinitionDTO.getProcessId();
        entity.setId(processId);
        entity.setName(processDefinitionDTO.getName());

        ProcessDefinitionEntity oldEntity = exist(processId);

        if(oldEntity != null) {
            processDefinitionDao.updateById(entity);
        } else {
            processDefinitionDao.insert(entity);
        }

        return processDefinitionDTO;
    }

    @Override
    public void delete(ProcessDefinitionDto processDefinitionDTO) {
        String processId = processDefinitionDTO.getProcessId();
        ProcessDefinitionEntity oldEntity = exist(processId);
        Assert.notNull(oldEntity, "流程不存在：{}", processId);
        processDefinitionDao.deleteById(processId);
    }

    @Override
    public ProcessDefinitionDto getOne(String processId) {
        Assert.notNull(processId, ErrorMessage.NOT_NULL("流程 id"));
        ProcessDefinitionEntity entity = exist(processId);
        Assert.notNull(entity, "流程id不存在：{}", processId);
        return toDTO(entity);
    }

    private ProcessDefinitionEntity exist(String processId) {
        QueryWrapper<ProcessDefinitionEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ProcessDefinitionEntity.ID, processId);
        return processDefinitionDao.selectOne(queryWrapper);
    }

    private ProcessDefinitionDto toDTO(ProcessDefinitionEntity entity) {
        ProcessDefinitionDto dto = new ProcessDefinitionDto();
        dto.setProcessId(entity.getId());
        dto.setCreateTime(LocalDateTimeUtil.of(entity.getCreateTime()));
        dto.setUpdateTime(LocalDateTimeUtil.of(entity.getUpdateTime()));
        dto.setName(entity.getName());
        dto.setXml(entity.getXml());
        dto.setImgUrl(entity.getImgUrl());
        return dto;
    }


}
