package com.brandnewdata.mop.poc.connector.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.common.pojo.BasePageResult;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.api.connector.IConnectorApi;
import com.brandnewdata.mop.api.connector.dto.*;
import com.brandnewdata.mop.poc.bff.converter.process.ProcessDefinitionVoConverter;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.connector.converter.ProcessInstanceDtoConverter;
import com.brandnewdata.mop.poc.constant.ProcessConst;
import com.brandnewdata.mop.poc.env.dto.EnvDto;
import com.brandnewdata.mop.poc.env.service.IEnvService;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDto;
import com.brandnewdata.mop.poc.operate.service.IProcessInstanceService2;
import com.brandnewdata.mop.poc.process.dto.BpmnXmlDto;
import com.brandnewdata.mop.poc.process.dto.DeployStatusDto;
import com.brandnewdata.mop.poc.process.dto.ProcessSnapshotDeployDto;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService2;
import com.brandnewdata.mop.poc.process.util.ProcessUtil;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
public class ConnectorApi implements IConnectorApi {

    private final IEnvService envService;

    private final IProcessDeployService2 processDeployService;

    private final IProcessInstanceService2 processInstanceService;

    public ConnectorApi(IEnvService envService,
                        IProcessDeployService2 processDeployService,
                        IProcessInstanceService2 processInstanceService) {
        this.envService = envService;
        this.processDeployService = processDeployService;
        this.processInstanceService = processInstanceService;
    }

    @Override
    public Result deploy(ConnectorResource resource) {
        Assert.notNull(resource, "连接器资源为空");

        List<BPMNResource> operates = resource.getOperates();
        List<BPMNResource> triggers = resource.getTriggers();
        Assert.isFalse(CollUtil.isEmpty(operates) && CollUtil.isEmpty(triggers),
                "操作和触发器不能都为空");
        List<EnvDto> envDtoList = envService.fetchEnvList();
        EnvDto debugEnvDto = envService.fetchDebugEnv();
        List<Long> envIdList = new ArrayList<>();

        // debug env 和 normal env 都需要发布连接器
        envIdList.add(debugEnvDto.getId());
        envDtoList.forEach(envDto -> envIdList.add(envDto.getId()));

        // 部署触发器
        if(CollUtil.isNotEmpty(triggers)) {
            for (BPMNResource trigger : triggers) {
                try {
                    BpmnXmlDto bpmnXmlDto = new BpmnXmlDto();
                    bpmnXmlDto.setProcessId(ProcessUtil.convertProcessId(trigger.getModelKey()));
                    bpmnXmlDto.setProcessName(StrUtil.format("【触发器】{}", trigger.getName()));
                    bpmnXmlDto.setProcessXml(trigger.getEditorXML());
                    processDeployService.releaseDeploy(bpmnXmlDto, envIdList, ProcessConst.PROCESS_BIZ_TYPE__TRIGGER);
                } catch (Exception e) {
                    throw new RuntimeException(StrUtil.format("【触发器】{} 部署异常: {}", trigger.getName(), e.getMessage()));
                }

            }
        }

        // 部署操作
        if(CollUtil.isNotEmpty(operates)) {
            for (BPMNResource operate : operates) {
                try {
                    BpmnXmlDto bpmnXmlDto = new BpmnXmlDto();
                    bpmnXmlDto.setProcessId(ProcessUtil.convertProcessId(operate.getModelKey()));
                    bpmnXmlDto.setProcessName(StrUtil.format("【操作】{}", operate.getName()));
                    bpmnXmlDto.setProcessXml(operate.getEditorXML());
                    processDeployService.releaseDeploy(bpmnXmlDto, envIdList, ProcessConst.PROCESS_BIZ_TYPE__OPERATE);
                } catch (Exception e) {
                    throw new RuntimeException(StrUtil.format("【操作】{} 部署异常: {}", operate.getName(), e.getMessage()));
                }
            }
        }

        return Result.OK();
    }

    @Override
    @Transactional
    public Result snapshotDeploy(ConnectorResource resource) {
        checkConnectorResource(resource);
        List<BPMNResource> triggers = Opt.ofNullable(resource.getTriggers()).orElse(ListUtil.empty());
        List<BPMNResource> operates = Opt.ofNullable(resource.getOperates()).orElse(ListUtil.empty());

        List<EnvDto> envDtoList = getEnvDtoList(true);
        List<Long> envIdList = envDtoList.stream().map(EnvDto::getId).collect(Collectors.toList());

        // 部署触发器
        for (BPMNResource trigger : triggers) {
            for (Long envId : envIdList) {
                try {
                    BpmnXmlDto bpmnXmlDto = getBpmnXmlDto(trigger, true);
                    processDeployService.snapshotDeploy2(bpmnXmlDto, envId, ProcessConst.PROCESS_BIZ_TYPE__TRIGGER);
                } catch (Exception e) {
                    throw new RuntimeException(StrUtil.format("【触发器】{} 部署异常: {}", trigger.getName(), e.getMessage()));
                }
            }
        }

        // 部署操作
        for (BPMNResource operate : operates) {
            for (Long envId : envIdList) {
                try {
                    BpmnXmlDto bpmnXmlDto = getBpmnXmlDto(operate, false);
                    processDeployService.snapshotDeploy2(bpmnXmlDto, envId, ProcessConst.PROCESS_BIZ_TYPE__OPERATE);
                } catch (Exception e) {
                    throw new RuntimeException(StrUtil.format("【操作】{} 部署异常: {}", operate.getName(), e.getMessage()));
                }
            }
        }

        return Result.OK();
    }

    @Override
    @Transactional
    public Result releaseDeploy(ConnectorResource resource) {
        checkConnectorResource(resource);
        List<BPMNResource> triggers = Opt.ofNullable(resource.getTriggers()).orElse(ListUtil.empty());
        List<BPMNResource> operates = Opt.ofNullable(resource.getOperates()).orElse(ListUtil.empty());

        List<EnvDto> envDtoList = getEnvDtoList(false);
        List<Long> envIdList = envDtoList.stream().map(EnvDto::getId).collect(Collectors.toList());

        // 部署触发器
        for (BPMNResource trigger : triggers) {
            for (Long envId : envIdList) {
                try {
                    BpmnXmlDto bpmnXmlDto = getBpmnXmlDto(trigger, true);
                    processDeployService.releaseDeploy2(bpmnXmlDto, envId, ProcessConst.PROCESS_BIZ_TYPE__TRIGGER);
                } catch (Exception e) {
                    throw new RuntimeException(StrUtil.format("【触发器】{} 部署异常: {}", trigger.getName(), e.getMessage()));
                }
            }
        }

        // 部署操作
        for (BPMNResource operate : operates) {
            for (Long envId : envIdList) {
                try {
                    BpmnXmlDto bpmnXmlDto = getBpmnXmlDto(operate, false);
                    processDeployService.releaseDeploy2(bpmnXmlDto, envId, ProcessConst.PROCESS_BIZ_TYPE__OPERATE);
                } catch (Exception e) {
                    throw new RuntimeException(StrUtil.format("【操作】{} 部署异常: {}", operate.getName(), e.getMessage()));
                }
            }
        }

        return Result.OK();
    }

    @Override
    public Result<ConnectorDeployProgressDto> fetchSnapshotDeployProgress(ConnectorResource resource) {
        checkConnectorResource(resource);
        List<BPMNResource> triggerList = resource.getTriggers();
        List<BPMNResource> operateList = resource.getOperates();

        List<EnvDto> envDtoList = getEnvDtoList(true);
        List<Long> envIdList = envDtoList.stream().map(EnvDto::getId).collect(Collectors.toList());

        ConnectorDeployProgressDto connectorDeployProgressDto = assembleConnectorDeployProgressDto(triggerList, operateList, envIdList);

        return Result.OK(connectorDeployProgressDto);
    }

    @Override
    public Result<ConnectorDeployProgressDto> fetchReleaseDeployProgress(ConnectorResource resource) {
        checkConnectorResource(resource);
        List<BPMNResource> triggerList = resource.getTriggers();
        List<BPMNResource> operateList = resource.getOperates();

        List<EnvDto> envDtoList = getEnvDtoList(false);
        List<Long> envIdList = envDtoList.stream().map(EnvDto::getId).collect(Collectors.toList());

        ConnectorDeployProgressDto connectorDeployProgressDto = assembleConnectorDeployProgressDto(triggerList, operateList, envIdList);

        return Result.OK(connectorDeployProgressDto);
    }

    @Override
    public Result<BasePageResult<ProcessInstanceDto>> fetchSnapshotProcessInstancePage(ProcessInstanceQueryDto queryDto) {
        BasePageResult<ProcessInstanceDto> ret = new BasePageResult<>();
        List<String> modelKeyList = queryDto.getModelKeyList();
        if(CollUtil.isEmpty(modelKeyList)) return Result.OK(ret);

        int pageNum = queryDto.getPageNum();
        int pageSize = queryDto.getPageSize();
        Assert.notNull(pageNum);
        Assert.notNull(pageSize);
        Assert.isFalse(CollUtil.hasNull(modelKeyList));

        Long envId = envService.fetchDebugEnv().getId();
        Map<String, String> processIdMap = modelKeyList.stream().collect(Collectors.toMap(ProcessUtil::convertProcessId, Function.identity()));

        // 获取流程定义
        Map<String, List<ProcessSnapshotDeployDto>> snapshotDeployMap =
                processDeployService.listSnapshotByEnvIdAndProcessId(envId, ListUtil.toList(processIdMap.keySet()));
        Map<Long, ProcessSnapshotDeployDto> processSnapshotDeployDtoMap = snapshotDeployMap.values().stream()
                .flatMap(Collection::stream).collect(Collectors.toMap(ProcessSnapshotDeployDto::getProcessZeebeKey, Function.identity()));

        // 根据流程定义去查询流程实例
        Page<ListViewProcessInstanceDto> page =
                processInstanceService.pageProcessInstanceByZeebeKey(
                        envId, ListUtil.toList(processSnapshotDeployDtoMap.keySet()), pageNum, pageSize, new HashMap<>());

        List<ProcessInstanceDto> dtoList = new ArrayList<>();
        for (ListViewProcessInstanceDto listViewProcessInstanceDto : page.getRecords()) {
            ProcessInstanceDto processInstanceDto = ProcessInstanceDtoConverter.createFrom(listViewProcessInstanceDto);
            Long zeebeKey = listViewProcessInstanceDto.getProcessId();
            String processId = listViewProcessInstanceDto.getBpmnProcessId();
            String modelKey = processIdMap.get(processId);
            // 连接器这边需要将 process id 转换为 model key
            processInstanceDto.setProcessId(modelKey);
            ProcessSnapshotDeployDto processSnapshotDeployDto = processSnapshotDeployDtoMap.get(zeebeKey);
            ProcessInstanceDtoConverter.updateFrom(processInstanceDto, processSnapshotDeployDto);
            dtoList.add(processInstanceDto);
        }

        ret.setTotal(page.getTotal());
        ret.setRecords(dtoList);
        return Result.OK(ret);
    }

    @Override
    public Result<String> fetchSnapshotProcessDefinition(Long snapshotDeployId) {

        Assert.notNull(snapshotDeployId, "部署id不能为空");
        ProcessSnapshotDeployDto processSnapshotDeployDto =
                processDeployService.listSnapshotById(ListUtil.of(snapshotDeployId)).get(snapshotDeployId);
        Assert.notNull(processSnapshotDeployDto, "部署id不存在: {}", snapshotDeployId);

        // 获取流程定义
        return Result.OK(processSnapshotDeployDto.getProcessXml());
    }

    @Override
    public Result startSnapshotProcessInstance(BPMNResource resource) {
        Long envId = envService.fetchDebugEnv().getId();
        String processId = ProcessUtil.convertProcessId(resource.getModelKey());
        processDeployService.startAsync(processId, Opt.ofNullable(resource.getVariables()).orElse(MapUtil.empty()), envId);
        return Result.OK();
    }

    private void checkConnectorResource(ConnectorResource resource) {
        Assert.notNull(resource, "连接器资源为空");

        List<BPMNResource> operates = resource.getOperates();
        List<BPMNResource> triggers = resource.getTriggers();
        Assert.isFalse(CollUtil.isEmpty(operates) && CollUtil.isEmpty(triggers),
                "操作和触发器不能都为空");
    }

    private List<EnvDto> getEnvDtoList(boolean isDebug) {
        List<EnvDto> envDtoList = envService.fetchEnvList();
        List<Long> envIdList = new ArrayList<>();
        envDtoList.forEach(envDto -> envIdList.add(envDto.getId()));

        if(isDebug) {
            EnvDto debugEnvDto = envService.fetchDebugEnv();
            envIdList.add(debugEnvDto.getId());
        }
        return envDtoList;
    }

    private BpmnXmlDto getBpmnXmlDto(BPMNResource resource, boolean isTrigger) {
        BpmnXmlDto ret = new BpmnXmlDto();
        ret.setProcessId(ProcessUtil.convertProcessId(resource.getModelKey()));
        if(isTrigger) {
            ret.setProcessName(StrUtil.format("【触发器】{}", resource.getName()));
        } else {
            ret.setProcessName(StrUtil.format("【操作】{}", resource.getName()));
        }
        ret.setProcessXml(resource.getEditorXML());
        return ret;
    }

    private ConnectorDeployProgressDto assembleConnectorDeployProgressDto(List<BPMNResource> triggerList,
                                                                          List<BPMNResource> operateList,
                                                                          List<Long> envIdList) {
        Map<String, ConnectorProcessDeployStatusDto> triggerDeployStatusMap = getResourceDeployStatusMap(triggerList, envIdList);
        Map<String, ConnectorProcessDeployStatusDto> operateDeployStatusMap = getResourceDeployStatusMap(operateList, envIdList);

        int status = 1;
        int totalCount = 0;
        int successCount = 0;
        List<String> errorMessages = new ArrayList<>();
        Collection<ConnectorProcessDeployStatusDto> allStatus = CollUtil.toCollection(triggerDeployStatusMap.values());
        allStatus.addAll(operateDeployStatusMap.values());

        for (ConnectorProcessDeployStatusDto statusDto : allStatus) {
            int _status = statusDto.getStatus();
            if(NumberUtil.equals(_status, ProcessConst.PROCESS_DEPLOY_STATUS__EXCEPTION)) {
                status = _status;

                Map<String, String> _errorMessageMap = statusDto.getErrorMessageMap();
                _errorMessageMap.forEach((envName, message) -> {
                    errorMessages.add(StrUtil.format("{}: {}", envName, message));
                });
            } else if (NumberUtil.equals(status, ProcessConst.PROCESS_DEPLOY_STATUS__DEPLOYED)) {
                status = _status;
            }

            totalCount++;
            if(NumberUtil.equals(_status, ProcessConst.PROCESS_DEPLOY_STATUS__DEPLOYED)) {
                successCount++;
            }
        }

        ConnectorDeployProgressDto ret = new ConnectorDeployProgressDto();
        ret.setStatus(status);
        ret.setErrorMessage(StrUtil.join("; ", errorMessages));
        ret.setProgressPercentage(NumberUtil.div(totalCount, successCount, 2));
        ret.setTriggerDeployStatusMap(triggerDeployStatusMap);
        ret.setOperateDeployStatusMap(operateDeployStatusMap);
        return ret;
    }

    private Map<String, ConnectorProcessDeployStatusDto> getResourceDeployStatusMap(List<BPMNResource> resourceList, List<Long> envIdList) {

        Map<String, String> triggerProcessIdMap = Opt.ofNullable(resourceList).orElse(ListUtil.empty()).stream()
                .map(BPMNResource::getModelKey).collect(Collectors.toMap(ProcessUtil::convertProcessId, Function.identity()));

        Map<String, ConnectorProcessDeployStatusDto> resourceDeployStatusMap = new HashMap<>();

        for (Long envId : Opt.ofNullable(envIdList).orElse(ListUtil.empty())) {
            Map<String, DeployStatusDto> deployStatusDtoMap =
                    processDeployService.fetchDeployStatus(ListUtil.toList(triggerProcessIdMap.keySet()), envId);
            Assert.isTrue(deployStatusDtoMap.size() == resourceList.size(), "资源尚未部署，无法查询状态");

            for (Map.Entry<String, DeployStatusDto> entry : deployStatusDtoMap.entrySet()) {
                String processId = entry.getKey();
                DeployStatusDto _deployStatusDto = entry.getValue();
                String _modelKey = triggerProcessIdMap.get(processId);
                ConnectorProcessDeployStatusDto connectorProcessDeployStatusDto =
                        resourceDeployStatusMap.computeIfAbsent(_modelKey, key -> new ConnectorProcessDeployStatusDto());
                if(_deployStatusDto.getStatus() == ProcessConst.PROCESS_DEPLOY_STATUS__EXCEPTION) {
                    // 如果当前部署是失败，则不管之前是什么状态，都修改为失败
                    connectorProcessDeployStatusDto.setStatus(_deployStatusDto.getStatus());

                    // 记录异常结果
                    EnvDto envDto = envService.fetchOne(envId);
                    Map<String, String> messageMap =
                            Opt.ofNullable(connectorProcessDeployStatusDto.getErrorMessageMap()).orElse(new LinkedHashMap<>());
                    messageMap.put(envDto.getName(), _deployStatusDto.getMessage());
                    connectorProcessDeployStatusDto.setErrorMessageMap(messageMap);
                } else if (Opt.ofNullable(connectorProcessDeployStatusDto.getStatus()).orElse(1) == 1) { // 默认赋值为 1
                    // 如果之前是成功部署的，则当前环境状态可以直接覆盖总状态
                    connectorProcessDeployStatusDto.setStatus(_deployStatusDto.getStatus());
                }
            }
        }

        return resourceDeployStatusMap;
    }

}
