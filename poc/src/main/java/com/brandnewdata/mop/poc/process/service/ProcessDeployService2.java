package com.brandnewdata.mop.poc.process.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.constant.ProcessConst;
import com.brandnewdata.mop.poc.error.ErrorMessage;
import com.brandnewdata.mop.poc.process.bo.ZeebeDeployBo;
import com.brandnewdata.mop.poc.process.converter.ProcessReleaseDeployConverter;
import com.brandnewdata.mop.poc.process.converter.ProcessSnapshotDeployConverter;
import com.brandnewdata.mop.poc.process.dao.ProcessReleaseDeployDao;
import com.brandnewdata.mop.poc.process.dao.ProcessSnapshotDeployDao;
import com.brandnewdata.mop.poc.process.dto.BizDeployDto;
import com.brandnewdata.mop.poc.process.manager.ConnectorManager;
import com.brandnewdata.mop.poc.process.manager.ZeebeClientManager;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParseStep1;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParseStep2;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParser;
import com.brandnewdata.mop.poc.process.parser.dto.Step1Result;
import com.brandnewdata.mop.poc.process.parser.dto.Step2Result;
import com.brandnewdata.mop.poc.process.po.ProcessReleaseDeployPo;
import com.brandnewdata.mop.poc.process.po.ProcessSnapshotDeployPo;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.Process;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ProcessDeployService2 implements IProcessDeployService2 {

    private final ZeebeClientManager zeebeClientManager;

    private final ProcessSnapshotDeployDao snapshotDeployDao;

    private final ProcessReleaseDeployDao releaseDeployDao;

    private final ConnectorManager connectorManager;

    public ProcessDeployService2(ZeebeClientManager zeebeClientManager,
                                 ProcessSnapshotDeployDao snapshotDeployDao,
                                 ProcessReleaseDeployDao releaseDeployDao,
                                 ConnectorManager connectorManager) {
        this.zeebeClientManager = zeebeClientManager;
        this.snapshotDeployDao = snapshotDeployDao;
        this.releaseDeployDao = releaseDeployDao;
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
        po = ProcessSnapshotDeployConverter.createFrom(bo);
        ProcessSnapshotDeployConverter.updateFrom(envId, bizDeployDto.getProcessXml(), po);
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
            query.eq(ProcessReleaseDeployPo.ENV_ID, envIdList);
            ProcessReleaseDeployPo po = releaseDeployDao.selectOne(query);
            ZeebeDeployBo bo;
            if(po == null) {
                bo = zeebeDeploy(bizDeployDto, envId, bizType);
                po = ProcessReleaseDeployConverter.createFrom(bo);
                po.setEnvId(envId);
                releaseDeployDao.insert(po);
            } else {
                bo = zeebeDeploy(po.getProcessZeebeXml(), envId);
                if(!StrUtil.equals(bo.getProcessId(), po.getProcessId())) {
                    throw new RuntimeException("zeebe xml's process id is not equal to biz xml' process id");
                }

                if(!NumberUtil.equals(bo.getZeebeKey(), po.getProcessZeebeKey())) {
                    log.warn("release deploy has been modified. process id: {}", bo.getProcessId());
                    ProcessReleaseDeployConverter.updateFrom(bo, po);
                    releaseDeployDao.updateById(po);
                }
            }
        }
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
