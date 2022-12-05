package com.brandnewdata.mop.poc.process.service;

import com.brandnewdata.mop.poc.process.dto.BpmnXmlDto;
import com.brandnewdata.mop.poc.process.dto.ProcessReleaseDeployDto;
import com.brandnewdata.mop.poc.process.dto.ProcessSnapshotDeployDto;

import java.util.List;
import java.util.Map;

public interface IProcessDeployService2 {

    void snapshotDeploy(BpmnXmlDto bpmnXmlDto, Long envId, String bizType);

    void releaseDeploy(BpmnXmlDto bpmnXmlDto, List<Long> envIdList, String bizType);

    // todo caiwillie 可以优化，可选择是否获取xml
    Map<String, List<ProcessSnapshotDeployDto>> listSnapshotByEnvIdAndProcessId(Long envId, List<String> processIdList);

    Map<Long, ProcessSnapshotDeployDto> listSnapshotById(List<Long> idList);

    Map<String, ProcessReleaseDeployDto> fetchReleaseByEnvIdAndProcessId(Long envId, List<String> processIdList);

    Map<String, Object> startSync(BpmnXmlDto bpmnXmlDto, Map<String, Object> values, Long envId, String bizType);

    void startAsync(String processId, Map<String, Object> values, Long envId);

}
