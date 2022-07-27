package com.brandnewdata.mop.poc.process.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.error.ErrorMessage;
import com.brandnewdata.mop.poc.manager.ConnectorManager;
import com.brandnewdata.mop.poc.process.ProcessConstants;
import com.brandnewdata.mop.poc.process.dao.ProcessDeployVersionDao;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinition;
import com.brandnewdata.mop.poc.process.dto.ProcessDeploy;
import com.brandnewdata.mop.poc.process.entity.ProcessDeployVersionEntity;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParseStep1;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParser;
import com.brandnewdata.mop.poc.service.ServiceUtil;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.ProcessInstanceResult;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ProcessDeployServiceImpl implements IProcessDeployService{

    @Resource
    private ProcessDeployVersionDao processDeployVersionDao;

    @Resource
    private ConnectorManager connectorManager;

    @Resource
    private ZeebeClient zeebe;

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

    private ProcessDeploy toDTO(ProcessDeployVersionEntity entity) {
        ProcessDeploy dto = new ProcessDeploy();
        dto.setProcessId(entity.getProcessId());
        dto.setProcessName(entity.getProcessName());
        dto.setXml(entity.getProcessXml());
        dto.setVersion(entity.getVersion());
        dto.setType(entity.getType());
        return dto;
    }

    @Override
    public ProcessDeploy deploy(ProcessDefinition processDefinition, int type) {
        ProcessDefinitionParseStep1 step1 = ProcessDefinitionParser.newInstance(processDefinition);

        ProcessDefinition _processDefinition = null;
        if(type == ProcessConstants.PROCESS_TYPE_SCENE) {
            _processDefinition = step1.replaceProperties(connectorManager).replaceStep1()
                    .replaceSceneStartEvent(connectorManager).buildTriggerProcessDefinition();
        } else if (type == ProcessConstants.PROCESS_TYPE_TRIGGER) {
            _processDefinition = step1.replaceProperties(connectorManager).replaceStep1().replaceTriggerStartEvent()
                    .buildTriggerProcessDefinition();
        } else if (type == ProcessConstants.PROCESS_TYPE_OPERATE) {
            _processDefinition = step1.replaceProperties(connectorManager).replaceStep1().replaceOperateStartEvent()
                    .buildTriggerProcessDefinition();
        } else {
            throw new IllegalArgumentException(ErrorMessage.CHECK_ERROR("触发器类型不支持", null));
        }

        String xml = processDefinition.getXml(); // xml 需要取原始的数据
        // process id 和 name 需要取解析后的
        String processId = _processDefinition.getProcessId();
        String name = _processDefinition.getName();

        // 调用 zeebe 部署
        DeploymentEvent deploymentEvent = zeebe.newDeployResourceCommand()
                .addResourceStringUtf8(_processDefinition.getXml(), // 取解析后的xml
                        ServiceUtil.convertModelKey(processId) + ".bpmn")
                .send()
                .join();

        long zeebeKey = deploymentEvent.getKey();

        ProcessDeployVersionEntity latestVersion = getLatestDeployVersion(processId);

        ProcessDeployVersionEntity entity = new ProcessDeployVersionEntity();
        entity.setProcessId(processId);
        entity.setProcessName(name);
        entity.setProcessXml(xml);
        entity.setZeebeKey(zeebeKey);
        // 设置版本
        entity.setVersion(latestVersion == null ? 0 : latestVersion.getVersion() + 1);
        entity.setType(type);

        processDeployVersionDao.insert(entity);

        return toDTO(entity);
    }


    @Override
    public Page<ProcessDeploy> page(int pageNum, int pageSize) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ProcessDeployVersionEntity> page =
                com.baomidou.mybatisplus.extension.plugins.pagination.Page.of(pageNum, pageSize);
        QueryWrapper<ProcessDeployVersionEntity> queryWrapper = new QueryWrapper<>();
        page = processDeployVersionDao.selectPage(page, queryWrapper);
        List<ProcessDeploy> list = new ArrayList<>();

        List<ProcessDeployVersionEntity> records = page.getRecords();

        if(CollUtil.isNotEmpty(records)) {
            for (ProcessDeployVersionEntity record : records) {
                ProcessDeploy dto = toDTO(record);
                list.add(dto);
            }
        }

        return new Page<>(page.getTotal(), list);
    }

    @Override
    public Map<String, Object> startWithResult(String processId, Map<String, Object> values) {
        ProcessInstanceResult result = zeebe.newCreateInstanceCommand()
                .bpmnProcessId(ServiceUtil.convertModelKey(processId)) // 使用处理过的 processId
                .latestVersion()
                .variables(values)
                .withResult()
                .send()
                .join();

        return result.getVariablesAsMap();
    }


}
