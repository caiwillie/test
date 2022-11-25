package com.brandnewdata.mop.poc.process.service;

import com.brandnewdata.mop.poc.process.dto.ZeebeDeployDto;

public interface IProcessDeployService2 {

    void snapshotDeploy(ZeebeDeployDto zeebeDeployDto, String bizType);

    void releaseDeploy(ZeebeDeployDto zeebeDeployDto, String bizType);

}
