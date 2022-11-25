package com.brandnewdata.mop.poc.process.service;

import com.brandnewdata.mop.poc.process.dto.BizDeployDto;

import java.util.List;

public interface IProcessDeployService2 {

    void snapshotDeploy(BizDeployDto bizDeployDto, Long envId, String bizType);

    void releaseDeploy(BizDeployDto bizDeployDto, List<Long> envIdList, String bizType);

}
