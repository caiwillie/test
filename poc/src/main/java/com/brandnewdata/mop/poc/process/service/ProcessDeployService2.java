package com.brandnewdata.mop.poc.process.service;

import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.constant.ProcessConst;
import com.brandnewdata.mop.poc.error.ErrorMessage;
import com.brandnewdata.mop.poc.process.dao.ProcessReleaseDeployDao;
import com.brandnewdata.mop.poc.process.dao.ProcessSnapshotDeployDao;
import com.brandnewdata.mop.poc.process.dto.ZeebeDeployDto;
import com.brandnewdata.mop.poc.process.manager.ConnectorManager;
import com.brandnewdata.mop.poc.process.manager.ZeebeClientManager;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParseStep1;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParseStep2;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParser;

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
    public void snapshotDeploy(ZeebeDeployDto zeebeDeployDto, String bizType) {

    }

    @Override
    public void releaseDeploy(ZeebeDeployDto zeebeDeployDto, String bizType) {

    }


    private void deploy(ZeebeDeployDto zeebeDeployDto, String bizType) {
        String processId = zeebeDeployDto.getProcessId();
        String processName = zeebeDeployDto.getProcessName();
        String processZeebeXml = zeebeDeployDto.getProcessZeebeXml();
        ProcessDefinitionParseStep1 step1 = ProcessDefinitionParser.step1(processId, processName, processZeebeXml);
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

    }

}
