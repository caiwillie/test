package com.brandnewdata.mop.poc.process.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.error.ErrorMessage;
import com.brandnewdata.mop.poc.manager.ConnectorManager;
import com.brandnewdata.mop.poc.process.ProcessConstants;
import com.brandnewdata.mop.poc.process.dao.ProcessDeployDao;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionDTO;
import com.brandnewdata.mop.poc.process.dto.ProcessDeployDTO;
import com.brandnewdata.mop.poc.process.dto.parser.TriggerProcessDefinitionDTO;
import com.brandnewdata.mop.poc.process.entity.ProcessDeployEntity;
import com.brandnewdata.mop.poc.process.parser.FeelUtil;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParseStep1;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParser;
import com.brandnewdata.mop.poc.process.util.ProcessUtil;
import com.dxy.library.json.jackson.JacksonUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.Process;
import io.camunda.zeebe.client.api.response.ProcessInstanceResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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


    private Optional<ProcessDeployEntity> exist(String processId, int version) {
        QueryWrapper<ProcessDeployEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ProcessDeployEntity.PROCESS_ID, processId);
        queryWrapper.eq(ProcessDeployEntity.VERSION, version);
        return Optional.ofNullable(processDeployDao.selectOne(queryWrapper));
    }

    private ProcessDeployDTO toDTO(ProcessDeployEntity entity) {
        if(entity == null) return null; //为空返回
        ProcessDeployDTO dto = new ProcessDeployDTO();
        dto.setId(entity.getId());
        dto.setCreateTime(entity.getCreateTime());
        dto.setProcessId(entity.getProcessId());
        dto.setProcessName(entity.getProcessName());
        dto.setXml(entity.getProcessXml());
        dto.setVersion(entity.getVersion());
        dto.setType(entity.getType());
        dto.setZeebeKey(entity.getZeebeKey());
        return dto;
    }

    @Override
    public ProcessDeployDTO deploy(ProcessDefinitionDTO processDefinitionDTO, int type) {
        ProcessDefinitionParseStep1 step1 = ProcessDefinitionParser.newInstance(processDefinitionDTO);

        TriggerProcessDefinitionDTO triggerProcessDefinition = null;
        if(type == ProcessConstants.PROCESS_TYPE_SCENE) {
            triggerProcessDefinition = step1.replaceProperties(connectorManager).replaceStep1()
                    .replaceSceneStartEvent(connectorManager).buildTriggerProcessDefinition();
        } else if (type == ProcessConstants.PROCESS_TYPE_TRIGGER) {
            triggerProcessDefinition = step1.replaceProperties(connectorManager).replaceStep1()
                    .replaceTriggerStartEvent(connectorManager).buildTriggerProcessDefinition();
        } else if (type == ProcessConstants.PROCESS_TYPE_OPERATE) {
            triggerProcessDefinition = step1.replaceProperties(connectorManager).replaceStep1()
                    .replaceOperateStartEvent().buildTriggerProcessDefinition();
        } else {
            throw new IllegalArgumentException(ErrorMessage.CHECK_ERROR("触发器类型不支持", null));
        }

        String xml = processDefinitionDTO.getXml(); // xml 需要取原始的数据
        // process id 和 name 需要取解析后的
        String processId = triggerProcessDefinition.getProcessId();
        String name = triggerProcessDefinition.getName();
        String zeebeXML = triggerProcessDefinition.getXml();

        // 调用 zeebe 部署
        DeploymentEvent deploymentEvent = zeebe.newDeployResourceCommand()
                .addResourceStringUtf8(zeebeXML, // 取解析后的xml
                        StrUtil.format("{}.bpmn", processId))
                .send()
                .join();

        // 只会部署一个process
        Optional<Process> zeebeProcess = deploymentEvent.getProcesses().stream()
                .filter(process -> StrUtil.equals(process.getBpmnProcessId(), ProcessUtil.convertProcessId(processId)))
                .findFirst();

        Assert.isTrue(zeebeProcess.isPresent(), "发布失败");

        Process process = zeebeProcess.get();
        long zeebeKey = process.getProcessDefinitionKey();
        int version = zeebeProcess.get().getVersion();

        Optional<ProcessDeployEntity> exist = exist(processId, version);
        ProcessDeployEntity entity = null;
        if(exist.isPresent()) {
            // 版本未改变
            entity = exist.get();
        } else {
            // 版本更新
            entity = new ProcessDeployEntity();
            entity.setProcessId(processId);
            entity.setProcessName(name);
            entity.setProcessXml(xml);
            // 设置版本, 初始版本为1
            entity.setVersion(version);
            entity.setType(type);
            entity.setZeebeKey(zeebeKey);
            entity.setZeebeXml(zeebeXML);

            processDeployDao.insert(entity);
        }



        if(type == ProcessConstants.PROCESS_TYPE_SCENE && triggerProcessDefinition.getTrigger() != null) {
            // 如果有场景发布，并且是自定义触发器时，需要保存监听配置
            connectorManager.saveRequestParams(triggerProcessDefinition);
        }

        return toDTO(entity);
    }

    @Override
    public Page<ProcessDeployDTO> page(int pageNum, int pageSize) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ProcessDeployEntity> page =
                com.baomidou.mybatisplus.extension.plugins.pagination.Page.of(pageNum, pageSize);
        QueryWrapper<ProcessDeployEntity> queryWrapper = new QueryWrapper<>();
        page = processDeployDao.selectPage(page, queryWrapper);
        List<ProcessDeployDTO> list = new ArrayList<>();

        List<ProcessDeployEntity> records = page.getRecords();

        if(CollUtil.isNotEmpty(records)) {
            for (ProcessDeployEntity record : records) {
                ProcessDeployDTO dto = toDTO(record);
                list.add(dto);
            }
        }

        return new Page<>(page.getTotal(), list);
    }

    @Override
    public Map<String, Object> startWithResult(String processId, Map<String, Object> values) {

        ProcessDeployEntity processDeployEntity = getLatestDeployVersion(processId);
        Assert.notNull(processDeployEntity, ErrorMessage.NOT_NULL("流程 id"), processId);

        ProcessDefinitionDTO processDefinitionDTO = new ProcessDefinitionDTO();
        processDefinitionDTO.setXml(processDeployEntity.getProcessXml());

        TriggerProcessDefinitionDTO triggerProcessDefinition = ProcessDefinitionParser.newInstance(processDefinitionDTO).replaceStep1()
                .replaceSceneStartEvent(connectorManager).buildTriggerProcessDefinition();

        // 解析 xml 后得到响应表达式
        ObjectNode responseParams = triggerProcessDefinition.getResponseParams();

        String expression = JacksonUtil.to(responseParams);
        ProcessInstanceResult result = zeebe.newCreateInstanceCommand()
                .bpmnProcessId(ProcessUtil.convertProcessId(processId)) // 使用处理过的 processId
                .latestVersion()
                .variables(Optional.ofNullable(values).orElse(MapUtil.empty()))
                .withResult()
                .send()
                .join();

        Map<String, Object> resultVariables = result.getVariablesAsMap();

        Object response = FeelUtil.evalExpression(expression, resultVariables);

        return FeelUtil.convertMap(response);
    }

    @Override
    public ProcessDeployDTO getOne(long deployId) {
        ProcessDeployEntity entity = processDeployDao.selectById(deployId);
        return toDTO(entity);
    }

}
