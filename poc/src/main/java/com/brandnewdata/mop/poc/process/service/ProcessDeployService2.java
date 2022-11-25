package com.brandnewdata.mop.poc.process.service;

import com.brandnewdata.mop.poc.process.dao.ProcessReleaseDeployDao;
import com.brandnewdata.mop.poc.process.dao.ProcessSnapshotDeployDao;
import com.brandnewdata.mop.poc.process.dto.ZeebeDeployDto;
import com.brandnewdata.mop.poc.process.manager.ZeebeClientManager;

public class ProcessDeployService2 implements IProcessDeployService2 {

    private final ZeebeClientManager zeebeClientManager;

    private final ProcessSnapshotDeployDao snapshotDeployDao;

    private final ProcessReleaseDeployDao releaseDeployDao;

    public ProcessDeployService2(ZeebeClientManager zeebeClientManager,
                                 ProcessSnapshotDeployDao snapshotDeployDao,
                                 ProcessReleaseDeployDao releaseDeployDao) {
        this.zeebeClientManager = zeebeClientManager;
        this.snapshotDeployDao = snapshotDeployDao;
        this.releaseDeployDao = releaseDeployDao;
    }

    @Override
    public void snapshotDeploy(ZeebeDeployDto zeebeDeployDto) {

    }

    @Override
    public void releaseDeploy(ZeebeDeployDto zeebeDeployDto) {

    }

}
