package com.brandnewdata.mop.poc.process.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.error.ErrorMessage;
import com.brandnewdata.mop.poc.manager.ConnectorManager;
import com.brandnewdata.mop.poc.process.ProcessConstants;
import com.brandnewdata.mop.poc.process.cache.DeployNoExpCache;
import com.brandnewdata.mop.poc.process.dao.ProcessDeployDao;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionDto;
import com.brandnewdata.mop.poc.process.dto.ProcessDeployDto;
import com.brandnewdata.mop.poc.process.parser.dto.Step3Result;
import com.brandnewdata.mop.poc.process.entity.ProcessDeployEntity;
import com.brandnewdata.mop.poc.process.parser.FeelUtil;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParseStep1;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParseStep2;
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
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProcessDeployServiceImpl implements IProcessDeployService{

    @Resource
    private ProcessDeployDao processDeployDao;

    @Resource
    private ConnectorManager connectorManager;

    @Resource
    private ZeebeClient zeebe;

    @Resource
    private DeployNoExpCache deployCache;

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

    private ProcessDeployDto toDto(ProcessDeployEntity entity) {
        if(entity == null) return null; //为空返回
        ProcessDeployDto dto = new ProcessDeployDto();
        dto.setId(entity.getId());
        dto.setCreateTime(LocalDateTimeUtil.of(entity.getCreateTime()));
        dto.setUpdateTime(LocalDateTimeUtil.of(entity.getUpdateTime()));
        dto.setProcessId(entity.getProcessId());
        dto.setProcessName(entity.getProcessName());
        dto.setXml(entity.getProcessXml());
        dto.setVersion(entity.getVersion());
        dto.setType(entity.getType());
        dto.setZeebeKey(entity.getZeebeKey());
        return dto;
    }

    @Override
    public ProcessDeployDto deploy(ProcessDefinitionDto dto, int type) {
        ProcessDefinitionParseStep1 step1 = ProcessDefinitionParser.step1(dto.getProcessId(), dto.getName(), dto.getXml());
        ProcessDefinitionParseStep2 step2 = step1.replServiceTask(true, connectorManager).replAttr().step2();

        if(type == ProcessConstants.PROCESS_TYPE_SCENE) {
            step2.replEleSceneSe(connectorManager);
        } else if (type == ProcessConstants.PROCESS_TYPE_TRIGGER) {
            step2.replEleTriggerSe(connectorManager);
        } else if (type == ProcessConstants.PROCESS_TYPE_OPERATE) {
            step2.replEleOperateSe();
        } else {
            throw new IllegalArgumentException(ErrorMessage.CHECK_ERROR("触发器类型不支持", null));
        }

        Step3Result step3Result = step2.step3().step3Result();

        String xml = dto.getXml(); // xml 需要取原始的数据
        // process id 和 name 需要取解析后的
        String processId = step3Result.getProcessId();
        String name = step3Result.getName();
        String zeebeXML = step3Result.getXml();

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
            // 已发布的流程，但是没有任何实例，如果新发布一个版本，可能会存在把空实例版本替换
            entity = exist.get();
        } else {
            // 版本不存在
            entity = new ProcessDeployEntity();
            entity.setProcessId(processId);
            // 设置版本, 初始版本为1
            entity.setVersion(version);
        }

        entity.setProcessName(name);
        entity.setProcessXml(xml);
        entity.setType(type);
        entity.setZeebeKey(zeebeKey);
        entity.setZeebeXml(zeebeXML);

        if(exist.isPresent()) {
            // 版本已存在， 更新
            processDeployDao.updateById(entity);
        } else {
            // 版本不存在， 新增
            processDeployDao.insert(entity);
        }

        if(type == ProcessConstants.PROCESS_TYPE_SCENE && step3Result.getTrigger() != null) {
            // 如果有场景发布，并且是自定义触发器时，需要保存监听配置
            connectorManager.saveRequestParams(step3Result);
        }

        return toDto(entity);
    }

    @Override
    public List<ProcessDeployDto> listByType(int type) {
        List<ProcessDeployDto> ret = new ArrayList<>();
        for (ProcessDeployDto processDeployDto : deployCache.asMap().values()) {
            if(processDeployDto.getType() == type) {
                ret.add(processDeployDto);
            }
        }
        return ret;
    }

    @Override
    public List<ProcessDeployDto> listByIdList(List<String> idList) {
        if(CollUtil.isEmpty(idList)) return ListUtil.empty();
        QueryWrapper<ProcessDeployEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(ProcessDeployEntity.ID, idList);
        List<ProcessDeployEntity> entities = processDeployDao.selectList(queryWrapper);
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }


    @Override
    public Page<ProcessDeployDto> page(int pageNum, int pageSize) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ProcessDeployEntity> page =
                com.baomidou.mybatisplus.extension.plugins.pagination.Page.of(pageNum, pageSize);
        QueryWrapper<ProcessDeployEntity> queryWrapper = new QueryWrapper<>();
        // 查询部署
        page = processDeployDao.selectPage(page, queryWrapper);
        List<ProcessDeployDto> list = new ArrayList<>();

        List<ProcessDeployEntity> records = page.getRecords();

        if(CollUtil.isNotEmpty(records)) {
            for (ProcessDeployEntity record : records) {
                ProcessDeployDto dto = toDto(record);
                list.add(dto);
            }
        }

        return new Page<>(page.getTotal(), list);
    }

    @Override
    public Map<String, Object> startWithResult(String processId, Map<String, Object> values) {

        ProcessDeployEntity processDeployEntity = getLatestDeployVersion(processId);
        Assert.notNull(processDeployEntity, ErrorMessage.NOT_NULL("流程 id"), processId);

        Step3Result step3Result = ProcessDefinitionParser
                .step1(null, null, processDeployEntity.getProcessXml()).replServiceTask(true, connectorManager)
                .step2().replEleSceneSe(connectorManager)
                .step3().step3Result();

        // 解析 xml 后得到响应表达式
        ObjectNode responseParams = step3Result.getResponseParams();

        String expression = "";
        if(responseParams != null) {
            // todo caiwillie 还要验证一下
            expression = JacksonUtil.to(responseParams);
        }
        log.info("start process response expression: {}, {}", expression, StrUtil.isNotBlank(expression));

        ProcessInstanceResult result = zeebe.newCreateInstanceCommand()
                .bpmnProcessId(ProcessUtil.convertProcessId(processId)) // 使用处理过的 processId
                .latestVersion()
                .variables(Optional.ofNullable(values).orElse(MapUtil.empty()))
                .withResult()
                .send()
                .join();


        Map<String, Object> resultVariables = result.getVariablesAsMap();
        log.info("start process result variables: {}", JacksonUtil.to(resultVariables));

        Object response = null;
        if(StrUtil.isNotBlank(expression)) {
            response = FeelUtil.evalExpression(expression, resultVariables);
        } else {
            // 如果表达式为空就返回特定字段的内容
            response = resultVariables.get(ProcessConstants.PROCESS_RESPONSE_VARIABLE_NAME);
        }

        log.info("start process response: {}", JacksonUtil.to(response));

        if(response == null) {
            return null;
        } else {
            // 转换成string，再反序列化成map
            return JacksonUtil.fromMap(JacksonUtil.to(response));
        }
    }

    @Override
    public Map<String, Object> startWithResultTest(String processId, Map<String, Object> values) {
        ProcessInstanceResult result = zeebe.newCreateInstanceCommand()
                .bpmnProcessId(ProcessUtil.convertProcessId(processId)) // 使用处理过的 processId
                .latestVersion()
                .variables(Optional.ofNullable(values).orElse(MapUtil.empty()))
                .withResult()
                .send()
                .join();


        Map<String, Object> resultVariables = result.getVariablesAsMap();
        log.info("start process result variables: {}", JacksonUtil.to(resultVariables));
        return resultVariables;
    }

    @Override
    public ProcessDeployDto getOne(long deployId) {
        ProcessDeployEntity entity = processDeployDao.selectById(deployId);
        return toDto(entity);
    }

}
