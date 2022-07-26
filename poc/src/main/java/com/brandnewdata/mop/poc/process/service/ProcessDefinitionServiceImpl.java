package com.brandnewdata.mop.poc.process.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.error.ErrorMessage;
import com.brandnewdata.mop.poc.process.ProcessConstants;
import com.brandnewdata.mop.poc.process.dao.ProcessDefinitionDao;
import com.brandnewdata.mop.poc.process.dao.ProcessDeployVersionDao;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinition;
import com.brandnewdata.mop.poc.process.dto.TriggerProcessDefinition;
import com.brandnewdata.mop.poc.process.entity.ProcessDefinitionEntity;
import com.brandnewdata.mop.poc.process.entity.ProcessDeployVersionEntity;
import com.brandnewdata.mop.poc.process.parser.ConnectorManager;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParseStep1;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParser;
import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProcessDefinitionServiceImpl implements IProcessDefinitionService{

    @Resource
    private ProcessDefinitionDao processDefinitionDao;

    @Resource
    private ProcessDeployVersionDao processDeployVersionDao;

    @Resource
    private ConnectorManager connectorManager;

    @Resource
    private ZeebeClient zeebe;

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
        ProcessDefinitionParseStep1 step1 = ProcessDefinitionParser.newInstance(processDefinition);

        TriggerProcessDefinition triggerProcessDefinition = null;
        if(type == ProcessConstants.PROCESS_TYPE_SCENE) {
            triggerProcessDefinition = step1.replaceProperties(connectorManager).replaceStep1()
                    .replaceSceneStartEvent(connectorManager).buildTriggerProcessDefinition();
        } else if (type == ProcessConstants.PROCESS_TYPE_TRIGGER) {
            triggerProcessDefinition = step1.replaceProperties(connectorManager).replaceStep1().replaceTriggerStartEvent()
                    .buildTriggerProcessDefinition();
        } else if (type == ProcessConstants.PROCESS_TYPE_OPERATE) {
            triggerProcessDefinition = step1.replaceProperties(connectorManager).replaceStep1().replaceOperateStartEvent()
                    .buildTriggerProcessDefinition();
        } else {
            throw new IllegalArgumentException(ErrorMessage.CHECK_ERROR("触发器类型不支持", null));
        }

        String processId = triggerProcessDefinition.getProcessId();
        String name = triggerProcessDefinition.getName();
        String xml = triggerProcessDefinition.getXml();

        zeebe.newDeployResourceCommand()
                .addResourceStringUtf8(xmldto.getZeebeXML(), xmldto.getModelKey() + ".bpmn")
                .send()
                .join();

        ProcessDeployVersionEntity latestVersion = getLatestDeployVersion(processId);

        return null;
    }


    private ProcessDeployVersionEntity getLatestDeployVersion(String processId) {
        QueryWrapper<ProcessDeployVersionEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ProcessDeployVersionEntity.PROCESS_ID, processId);
        queryWrapper.orderByDesc(ProcessDeployVersionEntity.VERSION);
        List<ProcessDeployVersionEntity> list = processDeployVersionDao.selectList(queryWrapper);

        if(CollUtil.isEmpty(list)) {
            return null;
        } else {
            return list.get(0);
        }
    }

    private ProcessDefinition toDTO(ProcessDefinitionEntity entity) {
        ProcessDefinition dto = new ProcessDefinition();
        dto.setProcessId(entity.getId());
        dto.setName(entity.getName());
        dto.setXml(entity.getXml());
        return dto;
    }

}
