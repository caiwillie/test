package com.brandnewdata.mop.poc.process.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.constant.ProcessConst;
import com.brandnewdata.mop.poc.error.ErrorMessage;
import com.brandnewdata.mop.poc.process.cache.DeployNoExpCache;
import com.brandnewdata.mop.poc.process.dao.ProcessDeployDao;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionDto;
import com.brandnewdata.mop.poc.process.dto.ProcessDeployDto;
import com.brandnewdata.mop.poc.process.manager.ConnectorManager;
import com.brandnewdata.mop.poc.process.parser.FeelUtil;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParseStep1;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParseStep2;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParser;
import com.brandnewdata.mop.poc.process.parser.dto.Step2Result;
import com.brandnewdata.mop.poc.process.po.ProcessDeployPo;
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

    // todo caiwillie
    private ZeebeClient zeebe;

    @Resource
    private DeployNoExpCache deployCache;

    @Override
    public ProcessDeployDto deploy(ProcessDefinitionDto dto, int type) {
        ProcessDefinitionParseStep1 step1 = ProcessDefinitionParser.step1(dto.getProcessId(), dto.getName(), dto.getXml());
        ProcessDefinitionParseStep2 step2 = step1.replServiceTask(true, connectorManager).replAttr().step2();

        if(type == ProcessConst.PROCESS_TYPE_SCENE) {
            step2.replEleSceneSe(connectorManager);
        } else if (type == ProcessConst.PROCESS_TYPE_TRIGGER) {
            step2.replEleTriggerSe(connectorManager);
        } else if (type == ProcessConst.PROCESS_TYPE_OPERATE) {
            step2.replEleOperateSe();
        } else {
            throw new IllegalArgumentException(ErrorMessage.CHECK_ERROR("触发器类型不支持", null));
        }

        Step2Result step2Result = step2.step2Result();

        String xml = dto.getXml(); // xml 需要取原始的数据
        // process id 和 name 需要取解析后的
        String processId = step2Result.getProcessId();
        String name = step2Result.getProcessName();
        String zeebeXML = step2Result.getZeebeXml();

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

        Optional<ProcessDeployPo> exist = exist(processId, version);
        ProcessDeployPo entity = null;
        if(exist.isPresent()) {
            // 已发布的流程，但是没有任何实例，如果新发布一个版本，可能会存在把空实例版本替换
            entity = exist.get();
        } else {
            // 版本不存在
            entity = new ProcessDeployPo();
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

        if(type == ProcessConst.PROCESS_TYPE_SCENE && step2Result.getTrigger() != null) {
            // 如果有场景发布，并且是自定义触发器时，需要保存监听配置
            // connectorManager.saveRequestParams(step2Result);
        }

        return new ProcessDeployDto().from(entity, true);
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
    public List<ProcessDeployDto> listByIdList(List<Long> idList) {
        if(CollUtil.isEmpty(idList)) return ListUtil.empty();
        QueryWrapper<ProcessDeployPo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(ProcessDeployPo.ID, idList);
        queryWrapper.select(ProcessDeployPo.class, tableFieldInfo -> !StrUtil.equalsAny(tableFieldInfo.getColumn(),
                ProcessDeployPo.PROCESS_XML, ProcessDeployPo.ZEEBE_XML));
        List<ProcessDeployPo> entities = processDeployDao.selectList(queryWrapper);
        return entities.stream().map(entity -> new ProcessDeployDto().from(entity, false)).collect(Collectors.toList());
    }

    @Override
    public Page<ProcessDeployDto> page(int pageNum, int pageSize) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ProcessDeployPo> page =
                com.baomidou.mybatisplus.extension.plugins.pagination.Page.of(pageNum, pageSize);
        QueryWrapper<ProcessDeployPo> queryWrapper = new QueryWrapper<>();
        // 查询部署
        page = processDeployDao.selectPage(page, queryWrapper);
        List<ProcessDeployDto> list = new ArrayList<>();

        List<ProcessDeployPo> records = page.getRecords();

        if(CollUtil.isNotEmpty(records)) {
            for (ProcessDeployPo record : records) {
                list.add(new ProcessDeployDto().from(record, false));
            }
        }

        return new Page<>(page.getTotal(), list);
    }

    @Override
    public Map<String, Object> startWithResult(String processId, Map<String, Object> values) {

        ProcessDeployPo processDeployEntity = getLatestDeployVersion(processId);
        Assert.notNull(processDeployEntity, ErrorMessage.NOT_NULL("流程 id"), processId);

        Step2Result step2Result = ProcessDefinitionParser
                .step1(null, null, processDeployEntity.getProcessXml()).replServiceTask(true, connectorManager)
                .step2()
                .replEleSceneSe(connectorManager)
                .step2Result();

        // 解析 xml 后得到响应表达式
        ObjectNode responseParams = step2Result.getResponseParams();

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
            response = resultVariables.get(ProcessConst.PROCESS_RESPONSE_VARIABLE_NAME);
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
        ProcessDeployPo entity = processDeployDao.selectById(deployId);
        return new ProcessDeployDto().from(entity, true);
    }

    private ProcessDeployPo getLatestDeployVersion(String processId) {
        QueryWrapper<ProcessDeployPo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ProcessDeployPo.PROCESS_ID, processId);
        queryWrapper.orderByDesc(ProcessDeployPo.VERSION);
        List<ProcessDeployPo> list = processDeployDao.selectList(queryWrapper);

        if(CollUtil.isEmpty(list)) {
            return null;
        } else {
            return list.get(0);
        }
    }

    private Optional<ProcessDeployPo> exist(String processId, int version) {
        QueryWrapper<ProcessDeployPo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ProcessDeployPo.PROCESS_ID, processId);
        queryWrapper.eq(ProcessDeployPo.VERSION, version);
        return Optional.ofNullable(processDeployDao.selectOne(queryWrapper));
    }

}
