package com.brandnewdata.mop.poc.process.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.error.ErrorMessage;
import com.brandnewdata.mop.poc.process.dao.ProcessDefinitionDao;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionDTO;
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
    public List<ProcessDefinitionDTO> list(List<String> ids, boolean withXML) {
        List<ProcessDefinitionDTO> ret = new ArrayList<>();

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
                ProcessDefinitionDTO dto = toDTO(entity);
                ret.add(dto);
            }
        }

        return ret;
    }

    @Override
    public ProcessDefinitionDTO save(ProcessDefinitionDTO processDefinitionDTO) {


        // dto to entity 逻辑特殊，不提取公共
        ProcessDefinitionEntity entity = new ProcessDefinitionEntity();
        entity.setImgUrl(processDefinitionDTO.getImgUrl());
        entity.setXml(processDefinitionDTO.getXml());

        // todo 返回修改的结构体不再用 processDefinition
        processDefinitionDTO = ProcessDefinitionParser.newInstance(processDefinitionDTO).buildProcessDefinition();
        String processId = processDefinitionDTO.getProcessId();
        entity.setId(processId);
        entity.setName(processDefinitionDTO.getName());

        if(getOne(processId) != null) {
             processDefinitionDao.updateById(entity);
        } else {
            processDefinitionDao.insert(entity);
        }

        return processDefinitionDTO;
    }

    @Override
    public ProcessDefinitionDTO getOne(String processId) {
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

    private ProcessDefinitionDTO toDTO(ProcessDefinitionEntity entity) {
        ProcessDefinitionDTO dto = new ProcessDefinitionDTO();
        dto.setProcessId(entity.getId());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());
        dto.setName(entity.getName());
        dto.setXml(entity.getXml());
        dto.setImgUrl(entity.getImgUrl());
        return dto;
    }


}
