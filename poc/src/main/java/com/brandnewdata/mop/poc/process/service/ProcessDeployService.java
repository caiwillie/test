package com.brandnewdata.mop.poc.process.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.constant.ProcessConst;
import com.brandnewdata.mop.poc.error.ErrorMessage;
import com.brandnewdata.mop.poc.process.bo.ZeebeDeployBo;
import com.brandnewdata.mop.poc.process.converter.ProcessDeployTaskPoConverter;
import com.brandnewdata.mop.poc.process.converter.ProcessReleaseDeployDtoConverter;
import com.brandnewdata.mop.poc.process.converter.ProcessSnapshotDeployDtoConverter;
import com.brandnewdata.mop.poc.process.dao.ProcessDeployTaskDao;
import com.brandnewdata.mop.poc.process.dao.ProcessReleaseDeployDao;
import com.brandnewdata.mop.poc.process.dao.ProcessSnapshotDeployDao;
import com.brandnewdata.mop.poc.process.dto.BpmnXmlDto;
import com.brandnewdata.mop.poc.process.dto.DeployStatusDto;
import com.brandnewdata.mop.poc.process.dto.ProcessReleaseDeployDto;
import com.brandnewdata.mop.poc.process.dto.ProcessSnapshotDeployDto;
import com.brandnewdata.mop.poc.process.lock.ProcessEnvLock;
import com.brandnewdata.mop.poc.process.manager.ConnectorManager;
import com.brandnewdata.mop.poc.process.manager.ZeebeClientManager;
import com.brandnewdata.mop.poc.process.parser.FeelUtil;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParseStep1;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParseStep2;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParser;
import com.brandnewdata.mop.poc.process.parser.dto.Step2Result;
import com.brandnewdata.mop.poc.process.po.ProcessDeployTaskPo;
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
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProcessDeployService implements IProcessDeployService {

    @Resource
    private ProcessSnapshotDeployDao snapshotDeployDao;

    @Resource
    private ProcessReleaseDeployDao releaseDeployDao;

    @Resource
    private ProcessDeployTaskDao processDeployTaskDao;

    private final ZeebeClientManager zeebeClientManager;

    private final ConnectorManager connectorManager;

    private final ProcessEnvLock processEnvLock;

    public ProcessDeployService(ZeebeClientManager zeebeClientManager,
                                ConnectorManager connectorManager,
                                ProcessEnvLock processEnvLock) {
        this.zeebeClientManager = zeebeClientManager;
        this.connectorManager = connectorManager;
        this.processEnvLock = processEnvLock;
    }

    @Override
    @Transactional
    public void snapshotDeploy2(BpmnXmlDto bpmnXmlDto, Long envId, String bizType) {
        Assert.notNull(envId);
        Step2Result step2Result = parseBpmnXmlDto(bpmnXmlDto, envId, bizType);
        String processXml = bpmnXmlDto.getProcessXml();
        String processId = step2Result.getProcessId();
        String processName = step2Result.getProcessName();
        String zeebeXml = step2Result.getZeebeXml();
        Long version = null;
        try {
            do {
                version = processEnvLock.lock(processId, envId);
                // 一直等待到
                ThreadUtil.sleep(400);
            } while(version == null);

            String processDigest = DigestUtil.md5Hex(processXml);
            QueryWrapper<ProcessSnapshotDeployPo> query = new QueryWrapper<>();
            query.eq(ProcessSnapshotDeployPo.ENV_ID, envId);
            query.eq(ProcessSnapshotDeployPo.PROCESS_ID, processId);
            query.orderByDesc(ProcessSnapshotDeployPo.PROCESS_ZEEBE_VERSION, ProcessSnapshotDeployPo.CREATE_TIME);
            query.last("limit 1");
            ProcessSnapshotDeployPo latestProcessSnapshotDeployPo = snapshotDeployDao.selectOne(query);

            if(latestProcessSnapshotDeployPo != null && StrUtil.equals(latestProcessSnapshotDeployPo.getProcessDigest(), processDigest)) {
                // 如果最新部署的版本和当前版本一致，就不需要部署了
                return;
            }

            QueryWrapper<ProcessDeployTaskPo> query2 = new QueryWrapper<>();
            query2.eq(ProcessDeployTaskPo.PROCESS_ID, processId);
            query2.eq(ProcessDeployTaskPo.ENV_ID, envId);
            query2.eq(ProcessDeployTaskPo.DEPLOY_STATUS, ProcessConst.PROCESS_DEPLOY_STATUS__UNDEPLOY);
            ProcessDeployTaskPo oldProcessDeployTaskPo = processDeployTaskDao.selectOne(query2);
            // delete old process undeploy task
            if(oldProcessDeployTaskPo != null) processDeployTaskDao.deleteById(oldProcessDeployTaskPo.getId());

            // add new process deploy task
            ProcessDeployTaskPo processDeployTaskPo = ProcessDeployTaskPoConverter.createFrom(envId, processId, processName, processXml, zeebeXml);
            processDeployTaskDao.insert(processDeployTaskPo);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            if(version != null) {
                processEnvLock.unlock(processId, envId, version);
            }
        }
    }

    @Override
    @Transactional
    public void releaseDeploy2(BpmnXmlDto bpmnXmlDto, Long envId, String bizType) {
        Assert.notNull(envId);
        Step2Result step2Result = parseBpmnXmlDto(bpmnXmlDto, envId, bizType);
        String processXml = bpmnXmlDto.getProcessXml();
        String processId = step2Result.getProcessId();
        String processName = step2Result.getProcessName();
        String zeebeXml = step2Result.getZeebeXml();
        Long version = null;
        try {
            do {
                version = processEnvLock.lock(processId, envId);
                // 一直等待到
                ThreadUtil.sleep(400);
            } while(version == null);

            QueryWrapper<ProcessReleaseDeployPo> query = new QueryWrapper<>();
            query.eq(ProcessReleaseDeployPo.PROCESS_ID, processId);
            query.eq(ProcessReleaseDeployPo.ENV_ID, envId);
            ProcessReleaseDeployPo po = releaseDeployDao.selectOne(query);
            if(po != null) {
                // 如果最新部署的版本和当前版本一致，就不需要部署了
                return;
            }

            QueryWrapper<ProcessDeployTaskPo> query2 = new QueryWrapper<>();
            query2.eq(ProcessDeployTaskPo.PROCESS_ID, processId);
            query2.eq(ProcessDeployTaskPo.ENV_ID, envId);
            query2.eq(ProcessDeployTaskPo.DEPLOY_STATUS, ProcessConst.PROCESS_DEPLOY_STATUS__UNDEPLOY);
            ProcessDeployTaskPo oldProcessDeployTaskPo = processDeployTaskDao.selectOne(query2);
            // delete old process undeploy task
            if(oldProcessDeployTaskPo != null) processDeployTaskDao.deleteById(oldProcessDeployTaskPo.getId());

            // add new process deploy task
            ProcessDeployTaskPo processDeployTaskPo = ProcessDeployTaskPoConverter.createFrom(envId, processId, processName, processXml, zeebeXml);
            processDeployTaskDao.insert(processDeployTaskPo);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            if(version != null) {
                processEnvLock.unlock(processId, envId, version);
            }
        }
    }

    @Override
    public Map<String, DeployStatusDto> fetchDeployStatus(List<String> processIdList, Long envId) {
        if(CollUtil.isEmpty(processIdList)) return MapUtil.empty();
        Assert.isFalse(CollUtil.hasNull(processIdList), "process id must not null");
        Assert.notNull(envId);

        QueryWrapper<ProcessDeployTaskPo> query = new QueryWrapper<>();
        query.in(ProcessDeployTaskPo.PROCESS_ID, processIdList);
        query.eq(ProcessDeployTaskPo.ENV_ID, envId);
        query.groupBy(ProcessDeployTaskPo.PROCESS_ID);
        query.select("max(id) as id");
        List<Map<String, Object>> records = processDeployTaskDao.selectMaps(query);
        List<Long> idList = records.stream().map(map -> (Long) map.get("id")).collect(Collectors.toList());

        if(CollUtil.isEmpty(idList)) return MapUtil.empty();
        QueryWrapper<ProcessDeployTaskPo> query2 = new QueryWrapper<>();
        query2.in(ProcessDeployTaskPo.ID, idList);
        List<ProcessDeployTaskPo> processDeployTaskPoList = processDeployTaskDao.selectList(query2);

        return processDeployTaskPoList.stream().collect(Collectors.toMap(ProcessDeployTaskPo::getProcessId, po -> {
            DeployStatusDto dto = new DeployStatusDto();
            dto.setStatus(po.getDeployStatus());
            dto.setMessage(po.getErrorMessage());
            return dto;
        }));
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
    public Map<String, Object> startSync(BpmnXmlDto bpmnXmlDto, Map<String, Object> values, Long envId, String bizType) {
        Assert.notNull(envId, "环境id不能为空");
        Assert.notNull(bizType, "环境类型不能为空");

        String processId = bpmnXmlDto.getProcessId();
        String expression = parseResponseExpression(bpmnXmlDto, bizType);
        log.info("process {} expresssion: {}", processId, expression);
        ZeebeClient zeebeClient = zeebeClientManager.getByEnvId(envId);

        ProcessInstanceResult result = zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId(ProcessUtil.convertProcessId(processId)) // 使用处理过的 processId
                .latestVersion()
                .variables(Opt.ofNullable(values).orElse(MapUtil.empty()))
                .withResult()
                .send()
                .join();

        long processInstanceId = result.getProcessInstanceKey();
        Map<String, Object> processVariables = result.getVariablesAsMap();
        log.info("process instance {} result variables: {}", processInstanceId, JacksonUtil.to(processVariables));

        Map<String, Object> resultMap = Opt.ofNullable(processVariables).orElse(new HashMap<>());
        if(StrUtil.isNotBlank(expression)) {
            Object expressionResult = FeelUtil.evalExpression(expression, processVariables);
            Map<String, Object> computeExpressResult = FeelUtil.convertMap(expressionResult);
            // 更新表达式计算结果
            if(computeExpressResult != null) resultMap.putAll(computeExpressResult);
        }

        log.info("start process synchronously: {}, resultMap: {}, envId {}",
                processId, JacksonUtil.to(resultMap), envId);

        return Opt.ofNullable(resultMap).orElse(MapUtil.empty());
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

        log.info("start process asynchronously: {}, envId {}", processId, envId);
    }

    private String parseResponseExpression(BpmnXmlDto bpmnXmlDto, String bizType) {
        String processId = bpmnXmlDto.getProcessId();
        Step2Result step2Result = ProcessDefinitionParser
                .step1(processId, bpmnXmlDto.getProcessName(), bpmnXmlDto.getProcessXml())
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

    private ZeebeDeployBo zeebeDeploy(BpmnXmlDto bpmnXmlDto, Long envId, String bizType) {
        Assert.notNull(envId, "环境id不能为空");
        Assert.notNull(bizType, "环境类型不能为空");
        String processName = bpmnXmlDto.getProcessName();
        ProcessDefinitionParseStep1 step1 = ProcessDefinitionParser.step1(bpmnXmlDto.getProcessId(),
                processName, bpmnXmlDto.getProcessXml());
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

        return zeebeDeploy(step2Result.getZeebeXml(), step2Result.getProcessId(), step2Result.getProcessName(), envId);
    }

    private Step2Result parseBpmnXmlDto(BpmnXmlDto bpmnXmlDto, Long envId, String bizType) {
        Assert.notNull(envId, "环境id不能为空");
        Assert.notNull(bizType, "环境类型不能为空");
        ProcessDefinitionParseStep1 step1 = ProcessDefinitionParser.step1(bpmnXmlDto.getProcessId(),
                bpmnXmlDto.getProcessName(), bpmnXmlDto.getProcessXml());
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

        return step2.step2Result();
    }

    private synchronized ZeebeDeployBo zeebeDeploy(String zeebeXml, String processId, String name, Long envId) {
        // 防止发送过快，导致超出zeebe最大请求数
        ThreadUtil.sleep(3000);
        ZeebeDeployBo ret = new ZeebeDeployBo();
        ZeebeClient zeebeClient = zeebeClientManager.getByEnvId(envId);

        // 调用 zeebe 部署
        DeploymentEvent deploymentEvent = zeebeClient.newDeployResourceCommand()
                .addResourceStringUtf8(zeebeXml, StrUtil.format("{}.bpmn", name))
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
