package com.brandnewdata.mop.poc.process.schduler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.Scheduler;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.constant.EnvConst;
import com.brandnewdata.mop.poc.constant.ProcessConst;
import com.brandnewdata.mop.poc.env.dto.EnvDto;
import com.brandnewdata.mop.poc.env.lock.EnvLock;
import com.brandnewdata.mop.poc.env.service.IEnvService;
import com.brandnewdata.mop.poc.process.bo.ZeebeDeployBo;
import com.brandnewdata.mop.poc.process.converter.ProcessReleaseDeployPoConverter;
import com.brandnewdata.mop.poc.process.converter.ProcessSnapshotDeployPoConverter;
import com.brandnewdata.mop.poc.process.dao.ProcessDeployTaskDao;
import com.brandnewdata.mop.poc.process.dao.ProcessReleaseDeployDao;
import com.brandnewdata.mop.poc.process.dao.ProcessSnapshotDeployDao;
import com.brandnewdata.mop.poc.process.lock.ProcessEnvLock;
import com.brandnewdata.mop.poc.process.manager.ZeebeClientManager;
import com.brandnewdata.mop.poc.process.po.ProcessDeployTaskPo;
import com.brandnewdata.mop.poc.process.po.ProcessReleaseDeployPo;
import com.brandnewdata.mop.poc.process.po.ProcessSnapshotDeployPo;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.Process;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class ProcessDeployScheduler {

    private final IEnvService envService;

    private final EnvLock envLock;

    private final ProcessEnvLock processEnvLock;

    private final ZeebeClientManager zeebeClientManager;

    @Resource
    private ProcessDeployTaskDao processDeployTaskDao;

    @Resource
    private ProcessSnapshotDeployDao processSnapshotDeployDao;

    @Resource
    private ProcessReleaseDeployDao processReleaseDeployDao;

    public ProcessDeployScheduler(IEnvService envService, EnvLock envLock,
                                  ProcessEnvLock processEnvLock, ZeebeClientManager zeebeClientManager,
                                  @Value("${brandnewdata.deploy-schedule.enable}") boolean enable) {
        this.envService = envService;
        this.envLock = envLock;
        this.processEnvLock = processEnvLock;
        this.zeebeClientManager = zeebeClientManager;
        if(!enable) return;
        Scheduler scheduler = new Scheduler();
        scheduler.setMatchSecond(true);
        scheduler.schedule("0/2 * * * * ?", (Runnable) this::scan);
        scheduler.setThreadExecutor(Executors.newSingleThreadExecutor());
        scheduler.start();
    }

    protected void scan() {
        List<ProcessDeployTaskPo> processDeployTaskPoList = null;
        do {
            QueryWrapper<ProcessDeployTaskPo> query = new QueryWrapper<>();
            query.eq(ProcessDeployTaskPo.DEPLOY_STATUS, ProcessConst.PROCESS_DEPLOY_STATUS__UNDEPLOY);
            query.last("limit 10");
            processDeployTaskPoList = processDeployTaskDao.selectList(query);
            if(CollUtil.isEmpty(processDeployTaskPoList)) break;

            Long envLockVersion = null;
            Long processEnvLockVersion = null;
            ProcessDeployTaskPo processDeployTaskPo = null;
            Long envId = null;
            String processId = null;
            for (int i = 0; i < processDeployTaskPoList.size(); i++) {
                try {
                    processDeployTaskPo = processDeployTaskPoList.get(i);
                    envId = processDeployTaskPo.getEnvId();
                    processId = processDeployTaskPo.getProcessId();
                    Assert.notNull(envId);
                    Assert.notNull(processId);

                    // ???????????????
                    envLockVersion = envLock.lock(envId);
                    if(envLockVersion == null) {
                        log.debug("env lock compete fail. {}", envId);
                        continue;
                    }

                    // ???????????????
                    processEnvLockVersion = this.processEnvLock.lock(processId, envId);
                    if(processEnvLockVersion == null) {
                        log.debug("process env lock compete fail. process {}, env {}", processId, envId);
                        envLock.unlock(envId, envLockVersion);
                        continue;
                    }

                    // ???????????????????????????????????????, ???????????????????????????????????????
                    if(NumberUtil.equals(processDeployTaskDao.selectById(processDeployTaskPo.getId()).getDeployStatus(),
                            ProcessConst.PROCESS_DEPLOY_STATUS__UNDEPLOY)) break;
                } catch (Exception e) {
                    log.error("get lock exception", e);
                    break;
                }
            }

            // ???????????????????????????????????????????????????????????????????????????, ???????????????
            if(envLockVersion == null || processEnvLockVersion == null) break;

            try {
                log.info("deploy resource. thread group {}, name {}, envLockVersion {}, processEnvLockVersion {}, task id {}",
                        ThreadUtil.currentThreadGroup().getName(), Thread.currentThread().getName(),
                        envLockVersion, processEnvLockVersion, processDeployTaskPo.getId());
                // ????????????????????? ??????????????????
                ThreadUtil.sleep(3000);

                EnvDto envDto = envService.fetchOne(envId);
                Assert.notNull(envDto, "env not found");
                Integer type = envDto.getType();
                Assert.isTrue(StrUtil.containsAny(type.toString(), String.valueOf(EnvConst.ENV_TYPE__SANDBOX), String.valueOf(EnvConst.ENV_TYPE__NORMAL)),
                        "env type not support");

                ZeebeClient zeebeClient = zeebeClientManager.getByEnvId(envId);

                String processName = processDeployTaskPo.getProcessName();
                String zeebeXml = processDeployTaskPo.getProcessZeebeXml();

                // ?????? zeebe ??????
                DeploymentEvent deploymentEvent = zeebeClient.newDeployResourceCommand()
                        .addResourceStringUtf8(zeebeXml, StrUtil.format("{}.bpmn", processName))
                        .send()
                        .join();

                // ??????????????????process
                Process zeebeProcess = deploymentEvent.getProcesses().get(0);
                ZeebeDeployBo zeebeDeployBo = new ZeebeDeployBo();
                zeebeDeployBo.setProcessId(zeebeProcess.getBpmnProcessId());
                zeebeDeployBo.setZeebeXml(zeebeXml);
                zeebeDeployBo.setZeebeKey(zeebeProcess.getProcessDefinitionKey());
                zeebeDeployBo.setZeebeVersion(zeebeProcess.getVersion());

                // ???????????????????????????????????????
                if(NumberUtil.equals(type, EnvConst.ENV_TYPE__SANDBOX)) {
                    String processXml = processDeployTaskPo.getProcessXml();
                    String processDigest = DigestUtil.md5Hex(processXml);
                    // ??????????????????????????????
                    ProcessSnapshotDeployPo po = ProcessSnapshotDeployPoConverter.createFrom(zeebeDeployBo);
                    ProcessSnapshotDeployPoConverter.updateFrom(po, envId, processDigest, processXml);
                    processSnapshotDeployDao.insert(po);
                } else if(NumberUtil.equals(type, EnvConst.ENV_TYPE__NORMAL)) {
                    // ??????????????????????????????
                    ProcessReleaseDeployPo po = ProcessReleaseDeployPoConverter.createFrom(zeebeDeployBo);
                    po.setEnvId(envId);
                    processReleaseDeployDao.insert(po);
                }

                // ????????????????????????
                processDeployTaskPo.setDeployStatus(ProcessConst.PROCESS_DEPLOY_STATUS__DEPLOYED);
            } catch (Exception e) {
                log.error("deploy process fail. processId: {}, envId: {}", processId, envId, e);
                // ????????????????????????
                processDeployTaskPo.setDeployStatus(ProcessConst.PROCESS_DEPLOY_STATUS__EXCEPTION);
                processDeployTaskPo.setErrorMessage(e.getMessage());
            } finally {
                try {
                    processDeployTaskDao.updateById(processDeployTaskPo);
                } catch (Exception e) {
                    log.error("update process deploy task exception. id {}", processDeployTaskPo.getId());
                }

                // ???????????????
                processEnvLock.unlock(processId, envId, processEnvLockVersion);
                // ???????????????
                envLock.unlock(envId, envLockVersion);
            }

        } while(true);
    }

}
