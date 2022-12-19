package com.brandnewdata.mop.poc.process.manager;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
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
import com.brandnewdata.mop.poc.process.po.ProcessDeployTaskPo;
import com.brandnewdata.mop.poc.process.po.ProcessReleaseDeployPo;
import com.brandnewdata.mop.poc.process.po.ProcessSnapshotDeployPo;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.Process;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class DeployManager {

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

    public DeployManager(IEnvService envService, EnvLock envLock,
                         ProcessEnvLock processEnvLock, ZeebeClientManager zeebeClientManager) {
        this.envService = envService;
        this.envLock = envLock;
        this.processEnvLock = processEnvLock;
        this.zeebeClientManager = zeebeClientManager;
        CronUtil.getScheduler().setMatchSecond(true);
        CronUtil.schedule("0/2 * * * * ?", getRunnale());
    }

    private Runnable getRunnale() {
        return () -> {
            System.out.println("hello")
        };
    }

    private void scan() {
        QueryWrapper<ProcessDeployTaskPo> query = new QueryWrapper<>();
        query.eq(ProcessDeployTaskPo.DEPLOY_STATUS, ProcessConst.PROCESS_DEPLOY_STATUS__UNDEPLOY);
        List<ProcessDeployTaskPo> processDeployTaskPoList = processDeployTaskDao.selectList(query);
        if(CollUtil.isEmpty(processDeployTaskPoList)) return;
        for (ProcessDeployTaskPo processDeployTaskPo : processDeployTaskPoList) {
            Long envId = processDeployTaskPo.getEnvId();
            String processId = processDeployTaskPo.getProcessId();

            // 获取环境锁
            Long envLockVersion = envLock.lock(envId);
            if(envLockVersion == null) {
                log.warn("env lock compete fail. {}", envId);
                continue;
            }
            // 获取流程锁
            Long processEnvLock = this.processEnvLock.lock(processId, envId);
            if(processEnvLock == null) {
                log.warn("process env lock compete fail. process {}, env {}", processId, envId);
                envLock.unlock(envId, envLockVersion);
                continue;
            }

            EnvDto envDto = envService.fetchOne(envId);
            Assert.notNull(envDto, "env not found. envId: " + envId);
            Integer type = envDto.getType();
            Assert.isTrue(StrUtil.containsAny(type.toString(), String.valueOf(EnvConst.ENV_TYPE__SANDBOX), String.valueOf(EnvConst.ENV_TYPE__NORMAL)),
                    "env type not support. envId: " + envId);

            ZeebeClient zeebeClient = zeebeClientManager.getByEnvId(envId);

            String processName = processDeployTaskPo.getProcessName();
            String zeebeXml = processDeployTaskPo.getProcessZeebeXml();

            // 调用 zeebe 部署
            DeploymentEvent deploymentEvent = zeebeClient.newDeployResourceCommand()
                    .addResourceStringUtf8(zeebeXml, StrUtil.format("{}.bpmn", processName))
                    .send()
                    .join();

            // 只会部署一个process
            Process zeebeProcess = deploymentEvent.getProcesses().get(0);
            ZeebeDeployBo zeebeDeployBo = new ZeebeDeployBo();
            zeebeDeployBo.setProcessId(zeebeProcess.getBpmnProcessId());
            zeebeDeployBo.setZeebeXml(zeebeXml);
            zeebeDeployBo.setZeebeKey(zeebeProcess.getProcessDefinitionKey());
            zeebeDeployBo.setZeebeVersion(zeebeProcess.getVersion());

            String processXml = processDeployTaskPo.getProcessXml();
            String processDigest = processDeployTaskPo.getProcessDigest();

            // 根据环境类型，判断部署类型
            if(NumberUtil.equals(type, EnvConst.ENV_TYPE__SANDBOX)) {
                // 沙箱环境，部署到沙箱
                ProcessSnapshotDeployPo po = ProcessSnapshotDeployPoConverter.createFrom(zeebeDeployBo);
                ProcessSnapshotDeployPoConverter.updateFrom(po, envId, processDigest, processXml);
                processSnapshotDeployDao.insert(po);
            } else if(NumberUtil.equals(type, EnvConst.ENV_TYPE__NORMAL)) {
                // 正式环境，部署到正式
                ProcessReleaseDeployPo po = ProcessReleaseDeployPoConverter.createFrom(zeebeDeployBo);
                po.setEnvId(envId);
                processReleaseDeployDao.insert(po);
            }

            // 更新流程部署状态


        }
        // 2. 扫描数据库
        // 3. 释放锁
    }

}
