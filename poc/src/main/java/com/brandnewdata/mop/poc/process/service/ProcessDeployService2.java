package com.brandnewdata.mop.poc.process.service;

import com.brandnewdata.mop.poc.process.dto.ZeebeDeployDto;
import com.brandnewdata.mop.poc.process.manager.ZeebeClientManager;

public class ProcessDeployService2 implements IProcessDeployService2 {

    private final ZeebeClientManager zeebeClientManager;



    public ProcessDeployService2(ZeebeClientManager zeebeClientManager) {
        this.zeebeClientManager = zeebeClientManager;
    }

    @Override
    public void snapshotDeploy(ZeebeDeployDto zeebeDeployDto) {

    }

    @Override
    public void releaseDeploy(ZeebeDeployDto zeebeDeployDto) {

    }

}
