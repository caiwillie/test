package com.brandnewdata.mop.poc.process.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.error.ErrorMessage;
import com.brandnewdata.mop.poc.process.dao.ProcessDefinitionDao;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinition;
import com.brandnewdata.mop.poc.process.entity.ProcessDefinitionEntity;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParser;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProcessDefinitionServiceImpl implements IProcessDefinitionService{

    @Resource
    private ProcessDefinitionDao processDefinitionDao;

    @Override
    public List<ProcessDefinition> list(List<String> ids) {
        List<ProcessDefinition> ret = new ArrayList<>();

        if(CollUtil.isNotEmpty(ids)) {
            QueryWrapper<ProcessDefinitionEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.in(ProcessDefinitionEntity.ID, ids);
            List<ProcessDefinitionEntity> entities = processDefinitionDao.selectList(queryWrapper);
            if(CollUtil.isNotEmpty(entities)) {
                for (ProcessDefinitionEntity entity : entities) {
                    ProcessDefinition dto = toDTO(entity);
                    ret.add(dto);
                }
            }
        }

        return ret;
    }

    @Override
    public ProcessDefinition save(ProcessDefinition processDefinition) {
        String xml = processDefinition.getXml();

        processDefinition = ProcessDefinitionParser.newInstance(processDefinition).buildProcessDefinition();

        // dto to entity 逻辑特殊，不提取公共
        ProcessDefinitionEntity entity = new ProcessDefinitionEntity();
        String processId = processDefinition.getProcessId();
        entity.setId(processId);
        entity.setName(processDefinition.getName());
        entity.setXml(xml);

        if(getOne(processId) != null) {
             processDefinitionDao.updateById(entity);
        } else {
            processDefinitionDao.insert(entity);
        }

        return processDefinition;
    }

    @Override
    public ProcessDefinition getOne(String processId) {
        Assert.notNull(processId, ErrorMessage.NOT_NULL("流程 id"));

        QueryWrapper<ProcessDefinitionEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ProcessDefinitionEntity.ID, processId);
        ProcessDefinitionEntity entity = processDefinitionDao.selectOne(queryWrapper);

        if(entity == null) {
            return null;
        } else {
            return toDTO(entity);
        }
    }

    @Override
    public ProcessDefinition deploy(ProcessDefinition processDefinition, int type) {
        return null;
    }


    private ProcessDefinition toDTO(ProcessDefinitionEntity entity) {
        ProcessDefinition dto = new ProcessDefinition();
        dto.setProcessId(entity.getId());
        dto.setName(entity.getName());
        dto.setXml(entity.getXml());
        return dto;
    }

}
