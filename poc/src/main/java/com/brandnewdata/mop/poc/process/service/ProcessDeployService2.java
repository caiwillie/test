package com.brandnewdata.mop.poc.process.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.constant.ProcessConst;
import com.brandnewdata.mop.poc.error.ErrorMessage;
import com.brandnewdata.mop.poc.process.bo.ZeebeDeployBo;
import com.brandnewdata.mop.poc.process.converter.ProcessReleaseDeployDtoConverter;
import com.brandnewdata.mop.poc.process.converter.ProcessReleaseDeployPoConverter;
import com.brandnewdata.mop.poc.process.converter.ProcessSnapshotDeployDtoConverter;
import com.brandnewdata.mop.poc.process.converter.ProcessSnapshotDeployPoConverter;
import com.brandnewdata.mop.poc.process.dao.ProcessReleaseDeployDao;
import com.brandnewdata.mop.poc.process.dao.ProcessSnapshotDeployDao;
import com.brandnewdata.mop.poc.process.dto.BizDeployDto;
import com.brandnewdata.mop.poc.process.dto.ProcessReleaseDeployDto;
import com.brandnewdata.mop.poc.process.dto.ProcessSnapshotDeployDto;
import com.brandnewdata.mop.poc.process.manager.ConnectorManager;
import com.brandnewdata.mop.poc.process.manager.ZeebeClientManager;
import com.brandnewdata.mop.poc.process.parser.FeelUtil;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParseStep1;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParseStep2;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParser;
import com.brandnewdata.mop.poc.process.parser.dto.Step1Result;
import com.brandnewdata.mop.poc.process.parser.dto.Step2Result;
import com.brandnewdata.mop.poc.process.po.ProcessReleaseDeployPo;
import com.brandnewdata.mop.poc.process.po.ProcessSnapshotDeployPo;
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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProcessDeployService2 implements IProcessDeployService2 {

    private final ZeebeClientManager zeebeClientManager;

    @Resource
    private ProcessSnapshotDeployDao snapshotDeployDao;

    @Resource
    private ProcessReleaseDeployDao releaseDeployDao;

    private final ConnectorManager connectorManager;

    public ProcessDeployService2(ZeebeClientManager zeebeClientManager,
                                 ConnectorManager connectorManager) {
        this.zeebeClientManager = zeebeClientManager;
        this.connectorManager = connectorManager;
    }

    @Override
    public void snapshotDeploy(BizDeployDto bizDeployDto, Long envId, String bizType) {
        ZeebeDeployBo bo = zeebeDeploy(bizDeployDto, envId, bizType);
        QueryWrapper<ProcessSnapshotDeployPo> query = new QueryWrapper<>();
        query.eq(ProcessSnapshotDeployPo.ENV_ID, envId);
        query.eq(ProcessSnapshotDeployPo.PROCESS_ZEEBE_KEY, bo.getZeebeKey());
        ProcessSnapshotDeployPo po = snapshotDeployDao.selectOne(query);
        if(po != null) {
            // 说明没有更改任何东西，上个版本已经存在
            return;
        }
        po = ProcessSnapshotDeployPoConverter.createFrom(bo);
        ProcessSnapshotDeployPoConverter.updateFrom(envId, bizDeployDto.getProcessXml(), po);
        snapshotDeployDao.insert(po);
    }

    @Override
    public void releaseDeploy(BizDeployDto bizDeployDto, List<Long> envIdList, String bizType) {
        if(CollUtil.isEmpty(envIdList)) return;
        for (Long envId : envIdList) {
            Step1Result step1Result = ProcessDefinitionParser.step1(bizDeployDto.getProcessId(),
                    bizDeployDto.getProcessName(), bizDeployDto.getProcessXml()).step1Result();

            QueryWrapper<ProcessReleaseDeployPo> query = new QueryWrapper<>();
            query.eq(ProcessReleaseDeployPo.PROCESS_ID, step1Result.getProcessId());
            query.eq(ProcessReleaseDeployPo.ENV_ID, envId);
            ProcessReleaseDeployPo po = releaseDeployDao.selectOne(query);
            ZeebeDeployBo bo;
            if(po == null) {
                bo = zeebeDeploy(bizDeployDto, envId, bizType);
                po = ProcessReleaseDeployPoConverter.createFrom(bo);
                po.setEnvId(envId);
                releaseDeployDao.insert(po);
            } else {
                bo = zeebeDeploy(po.getProcessZeebeXml(), envId);
                if(!StrUtil.equals(bo.getProcessId(), po.getProcessId())) {
                    throw new RuntimeException("zeebe xml's process id is not equal to biz xml' process id");
                }

                if(!NumberUtil.equals(bo.getZeebeKey(), po.getProcessZeebeKey())) {
                    log.warn("release deploy has been modified. process id: {}", bo.getProcessId());
                    ProcessReleaseDeployPoConverter.updateFrom(bo, po);
                    releaseDeployDao.updateById(po);
                }
            }
        }
    }

    @Override
    public Map<String, List<ProcessSnapshotDeployDto>> listSnapshotByEnvIdAndProcessId(Long envId, List<String> processIdList) {
        Assert.notNull(envId, "环境id不能为空");
        if(CollUtil.isEmpty(processIdList)) return MapUtil.empty();
        Assert.isFalse(CollUtil.hasNull(processIdList), "流程id不能为空");

        QueryWrapper<ProcessSnapshotDeployPo> query = new QueryWrapper<>();
        query.eq(ProcessSnapshotDeployPo.ENV_ID, envId);
        query.in(ProcessSnapshotDeployPo.PROCESS_ID, processIdList);
        List<ProcessSnapshotDeployPo> processSnapshotDeployPoList = snapshotDeployDao.selectList(query);
        return processSnapshotDeployPoList.stream().map(ProcessSnapshotDeployDtoConverter::createFrom)
                .collect(Collectors.groupingBy(ProcessSnapshotDeployDto::getProcessId));
    }

    @Override
    public Map<Long, ProcessSnapshotDeployDto> listSnapshotById(List<Long> idList) {
        if(CollUtil.isEmpty(idList)) return MapUtil.empty();
        Assert.isFalse(CollUtil.hasNull(idList), "快照部署id不能为空");

        QueryWrapper<ProcessSnapshotDeployPo> query = new QueryWrapper<>();
        query.in(ProcessSnapshotDeployPo.ID, idList);
        List<ProcessSnapshotDeployPo> processSnapshotDeployPoList = snapshotDeployDao.selectList(query);
        return processSnapshotDeployPoList.stream().map(ProcessSnapshotDeployDtoConverter::createFrom)
                .collect(Collectors.toMap(ProcessSnapshotDeployDto::getId, Function.identity()));
    }

    @Override
    public Map<String, ProcessReleaseDeployDto> fetchReleaseByEnvIdAndProcessId(Long envId, List<String> processIdList) {
        Assert.notNull(envId, "环境id不能为空");
        if(CollUtil.isEmpty(processIdList)) return MapUtil.empty();
        Assert.isFalse(CollUtil.hasNull(processIdList), "流程id不能为空");

        QueryWrapper<ProcessReleaseDeployPo> query = new QueryWrapper<>();
        query.eq(ProcessReleaseDeployPo.ENV_ID, envId);
        query.in(ProcessReleaseDeployPo.PROCESS_ID, processIdList);
        List<ProcessReleaseDeployPo> processReleaseDeployPoList = releaseDeployDao.selectList(query);
        return processReleaseDeployPoList.stream().map(ProcessReleaseDeployDtoConverter::createFrom)
                .collect(Collectors.toMap(ProcessReleaseDeployDto::getProcessId, Function.identity()));
    }

    @Override
    public Map<String, Object> startSync(BizDeployDto bizDeployDto, Map<String, Object> values, Long envId, String bizType) {
        Assert.notNull(envId, "环境id不能为空");
        Assert.notNull(bizType, "环境类型不能为空");

        String processId = bizDeployDto.getProcessId();
        String expression = parseResponseExpression(bizDeployDto, bizType);

        ZeebeClient zeebeClient = zeebeClientManager.getByEnvId(envId);

        ProcessInstanceResult result = zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId(ProcessUtil.convertProcessId(processId)) // 使用处理过的 processId
                .latestVersion()
                .variables(Opt.ofNullable(values).orElse(MapUtil.empty()))
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
            response = resultVariables;
        }

        log.info("start process synchronously: {}, response expression eval: {}", processId, JacksonUtil.to(response));

        if(response == null) {
            return null;
        } else {
            // 转换成string，再反序列化成map
            return JacksonUtil.fromMap(JacksonUtil.to(response));
        }
    }

    @Override
    public void startAsync(String processId, Map<String, Object> values, Long envId) {
        Assert.notNull(envId, "环境id不能为空");

        ZeebeClient zeebeClient = zeebeClientManager.getByEnvId(envId);

        zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId(ProcessUtil.convertProcessId(processId)) // 使用处理过的 processId
                .latestVersion()
                .variables(Opt.ofNullable(values).orElse(MapUtil.empty()))
                .withResult()
                .send();

        log.info("start process asynchronously: {}", processId);
    }

    private String parseResponseExpression(BizDeployDto bizDeployDto, String bizType) {
        String processId = bizDeployDto.getProcessId();
        Step2Result step2Result = ProcessDefinitionParser
                .step1(processId, bizDeployDto.getProcessName(), bizDeployDto.getProcessXml())
                .step2().replEleSceneSe(connectorManager).step2Result();

        // 解析 xml 后得到响应表达式
        ObjectNode responseParams = step2Result.getResponseParams();

        String expression = "";
        if(responseParams != null) {
            // todo caiwillie 还要验证一下
            expression = JacksonUtil.to(responseParams);
        }

        log.info("parse process: {}, response expression: {}", processId, expression);

        return expression;
    }


    private ZeebeDeployBo zeebeDeploy(BizDeployDto bizDeployDto, Long envId, String bizType) {
        Assert.notNull(envId, "环境id不能为空");
        Assert.notNull(bizType, "环境类型不能为空");
        ProcessDefinitionParseStep1 step1 = ProcessDefinitionParser.step1(bizDeployDto.getProcessId(),
                bizDeployDto.getProcessName(), bizDeployDto.getProcessXml());
        ProcessDefinitionParseStep2 step2 = step1.replServiceTask(true, connectorManager).replAttr().step2();

        if(StrUtil.equals(bizType, ProcessConst.PROCESS_BIZ_TYPE__SCENE)) {
            step2.replEleSceneSe(connectorManager);
        } else if (StrUtil.equals(bizType, ProcessConst.PROCESS_BIZ_TYPE__TRIGGER)) {
            step2.replEleTriggerSe(connectorManager);
        } else if (StrUtil.equals(bizType, ProcessConst.PROCESS_BIZ_TYPE__OPERATE)) {
            step2.replEleOperateSe();
        } else {
            throw new RuntimeException(ErrorMessage.CHECK_ERROR("业务类型不支持", null));
        }

        Step2Result step2Result = step2.step2Result();

        // process id 和 name 需要取解析后确认
        String zeebeXml = step2Result.getXml();

        return zeebeDeploy(zeebeXml, envId);
    }

    private ZeebeDeployBo zeebeDeploy(String zeebeXml, Long envId) {
        ZeebeDeployBo ret = new ZeebeDeployBo();
        ZeebeClient zeebeClient = zeebeClientManager.getByEnvId(envId);

        // 调用 zeebe 部署
        DeploymentEvent deploymentEvent = zeebeClient.newDeployResourceCommand()
                .addResourceStringUtf8(zeebeXml, StrUtil.format("{}.bpmn", IdUtil.simpleUUID()))
                .send()
                .join();

        // 只会部署一个process
        Process zeebeProcess = deploymentEvent.getProcesses().get(0);

        ret.setProcessId(zeebeProcess.getBpmnProcessId());
        ret.setZeebeXml(zeebeXml);
        ret.setZeebeKey(zeebeProcess.getProcessDefinitionKey());
        ret.setZeebeVersion(zeebeProcess.getVersion());
        return ret;
    }

}
