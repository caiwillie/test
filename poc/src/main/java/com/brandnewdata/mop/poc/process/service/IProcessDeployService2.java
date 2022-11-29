package com.brandnewdata.mop.poc.process.service;

import com.brandnewdata.mop.poc.process.dto.BizDeployDto;
import com.brandnewdata.mop.poc.process.dto.ProcessSnapshotDeployDto;

import java.util.List;
import java.util.Map;

public interface IProcessDeployService2 {

    void snapshotDeploy(BizDeployDto bizDeployDto, Long envId, String bizType);

    void releaseDeploy(BizDeployDto bizDeployDto, List<Long> envIdList, String bizType);

    Map<String, List<ProcessSnapshotDeployDto>> listSnapshotByEnvIdAndProcessId(Long envId, List<String> processIdList);

    Map<Long, ProcessSnapshotDeployDto> listSnapshotById(List<Long> idList);

    Map<String, Object> startSync(BizDeployDto bizDeployDto, Map<String, Object> values, Long envId, String bizType);

    void startAsync(BizDeployDto bizDeployDto, Map<String, Object> values, Long envId, String bizType);

}
