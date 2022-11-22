package com.brandnewdata.mop.poc.process.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.error.ErrorMessage;
import com.brandnewdata.mop.poc.process.dao.ProcessDefinitionDao;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionDto;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParser;
import com.brandnewdata.mop.poc.process.parser.dto.Step1Result;
import com.brandnewdata.mop.poc.process.po.ProcessDefinitionPo;
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

        QueryWrapper<ProcessDefinitionPo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(ProcessDefinitionPo.ID, ids);
        if(!withXML) {
            // 不需要 xml 的话，就不需要查询xml字段
            queryWrapper.select(ProcessDefinitionPo.class,
                    tableFieldInfo -> !StrUtil.equals(tableFieldInfo.getColumn(), ProcessDefinitionPo.XML));
        }

        List<ProcessDefinitionPo> entities = processDefinitionDao.selectList(queryWrapper);
        if(CollUtil.isNotEmpty(entities)) {
            for (ProcessDefinitionPo entity : entities) {
                ProcessDefinitionDto dto = toDTO(entity);
                ret.add(dto);
            }
        }

        return ret;
    }

    @Override
    public ProcessDefinitionDto save(ProcessDefinitionDto dto) {

        // dto to entity 逻辑特殊，不提取公共
        ProcessDefinitionPo entity = new ProcessDefinitionPo();
        entity.setImgUrl(dto.getImgUrl());
        entity.setXml(dto.getXml());

        // 这里主要是校验，并解析 id、name
        Step1Result step1Result = ProcessDefinitionParser
                .step1(dto.getProcessId(), dto.getName(), dto.getXml())
                .step1Result();
        String processId = step1Result.getProcessId();
        entity.setId(processId);
        entity.setName(step1Result.getName());

        ProcessDefinitionPo oldEntity = exist(processId);

        if(oldEntity != null) {
            processDefinitionDao.updateById(entity);
        } else {
            processDefinitionDao.insert(entity);
        }

        return dto;
    }

    @Override
    public void delete(ProcessDefinitionDto processDefinitionDTO) {
        String processId = processDefinitionDTO.getProcessId();
        ProcessDefinitionPo oldEntity = exist(processId);
        Assert.notNull(oldEntity, "流程不存在：{}", processId);
        processDefinitionDao.deleteById(processId);
    }

    @Override
    public ProcessDefinitionDto getOne(String processId) {
        Assert.notNull(processId, ErrorMessage.NOT_NULL("流程 id"));
        ProcessDefinitionPo entity = exist(processId);
        Assert.notNull(entity, "流程id不存在：{}", processId);
        return toDTO(entity);
    }

    private ProcessDefinitionPo exist(String processId) {
        QueryWrapper<ProcessDefinitionPo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ProcessDefinitionPo.ID, processId);
        return processDefinitionDao.selectOne(queryWrapper);
    }

    private ProcessDefinitionDto toDTO(ProcessDefinitionPo entity) {
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
