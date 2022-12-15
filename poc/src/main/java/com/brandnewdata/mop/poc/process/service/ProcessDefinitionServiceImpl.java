package com.brandnewdata.mop.poc.process.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.process.dao.ProcessDefinitionDao;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionDto;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParser;
import com.brandnewdata.mop.poc.process.parser.dto.Step1Result;
import com.brandnewdata.mop.poc.process.po.ProcessDefinitionPo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ProcessDefinitionServiceImpl implements IProcessDefinitionService {

    @Resource
    private ProcessDefinitionDao processDefinitionDao;

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
        entity.setName(step1Result.getProcessName());

        ProcessDefinitionPo oldEntity = exist(processId);

        if(oldEntity != null) {
            processDefinitionDao.updateById(entity);
        } else {
            processDefinitionDao.insert(entity);
        }

        return dto;
    }

    private ProcessDefinitionPo exist(String processId) {
        QueryWrapper<ProcessDefinitionPo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ProcessDefinitionPo.ID, processId);
        return processDefinitionDao.selectOne(queryWrapper);
    }
}
