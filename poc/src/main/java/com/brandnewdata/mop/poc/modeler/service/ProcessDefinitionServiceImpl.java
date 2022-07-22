package com.brandnewdata.mop.poc.modeler.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.modeler.dao.ProcessDefinitionDao;
import com.brandnewdata.mop.poc.modeler.dto.ProcessDefinition;
import com.brandnewdata.mop.poc.modeler.entity.ProcessDefinitionEntity;
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
                    ProcessDefinition dto = toDto(entity);
                    ret.add(dto);
                }
            }
        }

        return ret;
    }

    @Override
    public ProcessDefinition save(String processId, String name, String xml) {

        return null;
    }


    private ProcessDefinition toDto(ProcessDefinitionEntity entity) {
        ProcessDefinition dto = new ProcessDefinition();
        dto.setProcessId(entity.getId());
        dto.setName(entity.getName());
        dto.setXml(entity.getXml());
        return dto;
    }

}
