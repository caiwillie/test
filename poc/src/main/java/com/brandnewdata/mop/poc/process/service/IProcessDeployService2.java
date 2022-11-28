package com.brandnewdata.mop.poc.process.service;

import com.brandnewdata.mop.poc.process.dto.BizDeployDto;
import com.brandnewdata.mop.poc.process.dto.ProcessSnapshotDeployDto;

import java.util.List;
import java.util.Map;

public interface IProcessDeployService2 {

    void snapshotDeploy(BizDeployDto bizDeployDto, Long envId, String bizType);

    void releaseDeploy(BizDeployDto bizDeployDto, List<Long> envIdList, String bizType);

    Map<String, List<ProcessSnapshotDeployDto>> listSnapshotByProcessIdAndEnvId(Long envId, List<String> processIdList);

}
