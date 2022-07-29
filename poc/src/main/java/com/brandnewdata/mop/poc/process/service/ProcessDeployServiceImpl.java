package com.brandnewdata.mop.poc.process.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.error.ErrorMessage;
import com.brandnewdata.mop.poc.manager.ConnectorManager;
import com.brandnewdata.mop.poc.process.ProcessConstants;
import com.brandnewdata.mop.poc.process.dao.ProcessDeployDao;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinition;
import com.brandnewdata.mop.poc.process.dto.ProcessDeploy;
import com.brandnewdata.mop.poc.process.dto.TriggerProcessDefinition;
import com.brandnewdata.mop.poc.process.entity.ProcessDeployEntity;
import com.brandnewdata.mop.poc.process.parser.FeelUtil;
import com.brandnewdata.mop.poc.process.parser.FeelVariablesJsonFactory;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParseStep1;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParser;
import com.brandnewdata.mop.poc.service.ServiceUtil;
import com.dxy.library.json.jackson.JacksonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.module.scala.DefaultScalaModule$;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.ProcessInstanceResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ProcessDeployServiceImpl implements IProcessDeployService{

    @Resource
    private ProcessDeployDao processDeployDao;

    @Resource
    private ConnectorManager connectorManager;

    @Resource
    private ZeebeClient zeebe;

    private ProcessDeployEntity getLatestDeployVersion(String processId) {
        QueryWrapper<ProcessDeployEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ProcessDeployEntity.PROCESS_ID, processId);
        queryWrapper.orderByDesc(ProcessDeployEntity.VERSION);
        List<ProcessDeployEntity> list = processDeployDao.selectList(queryWrapper);

        if(CollUtil.isEmpty(list)) {
            return null;
        } else {
            return list.get(0);
        }
    }

    private ProcessDeploy toDTO(ProcessDeployEntity entity) {
        if(entity == null) return null; //为空返回
        ProcessDeploy dto = new ProcessDeploy();
        dto.setId(entity.getId());
        dto.setCreateTime(LocalDateTimeUtil.formatNormal(entity.getCreateTime()));
        dto.setProcessId(entity.getProcessId());
        dto.setProcessName(entity.getProcessName());
        dto.setXml(entity.getProcessXml());
        dto.setVersion(entity.getVersion());
        dto.setType(entity.getType());
        dto.setZeebeKey(entity.getZeebeKey());
        return dto;
    }

    @Override
    public ProcessDeploy deploy(ProcessDefinition processDefinition, int type) {
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

        String xml = processDefinition.getXml(); // xml 需要取原始的数据
        // process id 和 name 需要取解析后的
        String processId = triggerProcessDefinition.getProcessId();
        String name = triggerProcessDefinition.getName();
        String zeebeXML = triggerProcessDefinition.getXml();

        // 调用 zeebe 部署
        DeploymentEvent deploymentEvent = zeebe.newDeployResourceCommand()
                .addResourceStringUtf8(zeebeXML, // 取解析后的xml
                         "a.bpmn")
                .send()
                .join();

        long zeebeKey = deploymentEvent.getKey();

        ProcessDeployEntity latestVersion = getLatestDeployVersion(processId);

        ProcessDeployEntity entity = new ProcessDeployEntity();
        entity.setProcessId(processId);
        entity.setProcessName(name);
        entity.setProcessXml(xml);
        // 设置版本, 初始版本为1
        entity.setVersion(latestVersion == null ? 1 : latestVersion.getVersion() + 1);
        entity.setType(type);
        entity.setZeebeKey(zeebeKey);
        entity.setZeebeXml(zeebeXML);

        processDeployDao.insert(entity);

        if(type == ProcessConstants.PROCESS_TYPE_SCENE) {
            // 如果有场景发布，需要保存监听配置
            connectorManager.saveRequestParams(triggerProcessDefinition);
        }

        return toDTO(entity);
    }


    @Override
    public Page<ProcessDeploy> page(int pageNum, int pageSize) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ProcessDeployEntity> page =
                com.baomidou.mybatisplus.extension.plugins.pagination.Page.of(pageNum, pageSize);
        QueryWrapper<ProcessDeployEntity> queryWrapper = new QueryWrapper<>();
        page = processDeployDao.selectPage(page, queryWrapper);
        List<ProcessDeploy> list = new ArrayList<>();

        List<ProcessDeployEntity> records = page.getRecords();

        if(CollUtil.isNotEmpty(records)) {
            for (ProcessDeployEntity record : records) {
                ProcessDeploy dto = toDTO(record);
                list.add(dto);
            }
        }

        return new Page<>(page.getTotal(), list);
    }

    @Override
    public Map<String, Object> startWithResult(String processId, Map<String, Object> values) {

        ProcessDeployEntity processDeployEntity = getLatestDeployVersion(processId);
        Assert.notNull(processDeployEntity, ErrorMessage.NOT_NULL("流程 id"), processId);

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setXml(processDeployEntity.getProcessXml());

        TriggerProcessDefinition triggerProcessDefinition = ProcessDefinitionParser.newInstance(processDefinition).replaceStep1()
                .replaceSceneStartEvent(connectorManager).buildTriggerProcessDefinition();

        // 解析 xml 后得到响应表达式
        ObjectNode responseParams = triggerProcessDefinition.getResponseParams();

        String expression = responseParams == null ? "" : JacksonUtil.to(responseParams);
        ProcessInstanceResult result = zeebe.newCreateInstanceCommand()
                .bpmnProcessId(ServiceUtil.convertModelKey(processId)) // 使用处理过的 processId
                .latestVersion()
                .variables(values)
                .withResult()
                .send()
                .join();

        Map<String, Object> resultVariables = result.getVariablesAsMap();

        Object response = FeelUtil.evalExpression(expression, resultVariables);

        return FeelUtil.convertMap(response);
    }

    @Override
    public ProcessDeploy getOne(long deployId) {
        ProcessDeployEntity entity = processDeployDao.selectById(deployId);
        return toDTO(entity);
    }




}
