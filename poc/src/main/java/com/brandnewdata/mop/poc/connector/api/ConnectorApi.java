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
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.connector.converter.ProcessInstanceDtoConverter;
import com.brandnewdata.mop.poc.constant.ProcessConst;
import com.brandnewdata.mop.poc.env.dto.EnvDto;
import com.brandnewdata.mop.poc.env.service.IEnvService;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDto;
import com.brandnewdata.mop.poc.operate.dto.filter.ProcessInstanceFilter;
import com.brandnewdata.mop.poc.operate.service.IProcessInstanceService;
import com.brandnewdata.mop.poc.process.dto.BpmnXmlDto;
import com.brandnewdata.mop.poc.process.dto.DeployStatusDto;
import com.brandnewdata.mop.poc.process.dto.ProcessSnapshotDeployDto;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService;
import com.brandnewdata.mop.poc.process.util.ProcessUtil;
import com.dxy.library.json.jackson.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class ConnectorApi implements IConnectorApi {

    private final IEnvService envService;

    private final IProcessDeployService processDeployService;

    private final IProcessInstanceService processInstanceService;

    public ConnectorApi(IEnvService envService,
                        IProcessDeployService processDeployService,
                        IProcessInstanceService processInstanceService) {
        this.envService = envService;
        this.processDeployService = processDeployService;
        this.processInstanceService = processInstanceService;
    }

    @Override
    @Transactional
    public Result snapshotDeploy(ConnectorResource resource) {
        checkConnectorResource(resource);
        List<BPMNResource> triggers = Opt.ofNullable(resource.getTriggers()).orElse(ListUtil.empty());
        List<BPMNResource> operates = Opt.ofNullable(resource.getOperates()).orElse(ListUtil.empty());

        List<EnvDto> envDtoList = getEnvDtoList(true);
        List<Long> envIdList = envDtoList.stream().map(EnvDto::getId).collect(Collectors.toList());

        // ???????????????
        for (BPMNResource trigger : triggers) {
            for (Long envId : envIdList) {
                try {
                    BpmnXmlDto bpmnXmlDto = getBpmnXmlDto(trigger, true);
                    processDeployService.snapshotDeploy(bpmnXmlDto, envId, ProcessConst.PROCESS_BIZ_TYPE__TRIGGER);
                } catch (Exception e) {
                    throw new RuntimeException(StrUtil.format("???????????????{} ????????????: {}", trigger.getName(), e.getMessage()));
                }
            }
        }

        // ????????????
        for (BPMNResource operate : operates) {
            for (Long envId : envIdList) {
                try {
                    BpmnXmlDto bpmnXmlDto = getBpmnXmlDto(operate, false);
                    processDeployService.snapshotDeploy(bpmnXmlDto, envId, ProcessConst.PROCESS_BIZ_TYPE__OPERATE);
                } catch (Exception e) {
                    throw new RuntimeException(StrUtil.format("????????????{} ????????????: {}", operate.getName(), e.getMessage()));
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

        // ???????????????
        for (BPMNResource trigger : triggers) {
            for (Long envId : envIdList) {
                try {
                    BpmnXmlDto bpmnXmlDto = getBpmnXmlDto(trigger, true);
                    processDeployService.releaseDeploy(bpmnXmlDto, envId, ProcessConst.PROCESS_BIZ_TYPE__TRIGGER);
                } catch (Exception e) {
                    throw new RuntimeException(StrUtil.format("???????????????{} ????????????: {}", trigger.getName(), e.getMessage()));
                }
            }
        }

        // ????????????
        for (BPMNResource operate : operates) {
            for (Long envId : envIdList) {
                try {
                    BpmnXmlDto bpmnXmlDto = getBpmnXmlDto(operate, false);
                    processDeployService.releaseDeploy(bpmnXmlDto, envId, ProcessConst.PROCESS_BIZ_TYPE__OPERATE);
                } catch (Exception e) {
                    throw new RuntimeException(StrUtil.format("????????????{} ????????????: {}", operate.getName(), e.getMessage()));
                }
            }
        }

        return Result.OK();
    }

    @Override
    public Result<ConnectorDeployProgressDto> fetchSnapshotDeployProgress(ConnectorResource resource) {
        log.debug("ConnectorApi.fetchSnapshotDeployProgress, resource: {}", JacksonUtil.to(resource));
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
        log.debug("ConnectorApi.fetchReleaseDeployProgress, resource: {}", JacksonUtil.to(resource));
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

        // ??????????????????
        Map<String, List<ProcessSnapshotDeployDto>> snapshotDeployMap =
                processDeployService.listSnapshotByEnvIdAndProcessId(envId, ListUtil.toList(processIdMap.keySet()));
        Map<Long, ProcessSnapshotDeployDto> processSnapshotDeployDtoMap = snapshotDeployMap.values().stream()
                .flatMap(Collection::stream).collect(Collectors.toMap(ProcessSnapshotDeployDto::getProcessZeebeKey, Function.identity(), (a, b) -> b));

        // ???????????????????????????????????????
        ProcessInstanceFilter processInstanceFilter = new ProcessInstanceFilter();
        Page<ListViewProcessInstanceDto> page =
                processInstanceService.pageProcessInstanceByZeebeKey(
                        envId, ListUtil.toList(processSnapshotDeployDtoMap.keySet()), pageNum, pageSize, processInstanceFilter, new HashMap<>());

        List<ProcessInstanceDto> dtoList = new ArrayList<>();
        for (ListViewProcessInstanceDto listViewProcessInstanceDto : page.getRecords()) {
            ProcessInstanceDto processInstanceDto = ProcessInstanceDtoConverter.createFrom(listViewProcessInstanceDto);
            Long zeebeKey = listViewProcessInstanceDto.getProcessId();
            String processId = listViewProcessInstanceDto.getBpmnProcessId();
            String modelKey = processIdMap.get(processId);
            // ???????????????????????? process id ????????? model key
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

        Assert.notNull(snapshotDeployId, "??????id????????????");
        ProcessSnapshotDeployDto processSnapshotDeployDto =
                processDeployService.listSnapshotById(ListUtil.of(snapshotDeployId)).get(snapshotDeployId);
        Assert.notNull(processSnapshotDeployDto, "??????id?????????: {}", snapshotDeployId);

        // ??????????????????
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
        Assert.notNull(resource, "?????????????????????");

        List<BPMNResource> operates = resource.getOperates();
        List<BPMNResource> triggers = resource.getTriggers();
        Assert.isFalse(CollUtil.isEmpty(operates) && CollUtil.isEmpty(triggers),
                "?????????????????????????????????");
    }

    private List<EnvDto> getEnvDtoList(boolean isDebug) {
        List<EnvDto> envDtoList = new ArrayList<>();
        if(!isDebug) {
            envDtoList.addAll(envService.fetchEnvList());
        }

        EnvDto debugEnvDto = envService.fetchDebugEnv();
        envDtoList.add(debugEnvDto);
        return envDtoList;
    }

    private BpmnXmlDto getBpmnXmlDto(BPMNResource resource, boolean isTrigger) {
        BpmnXmlDto ret = new BpmnXmlDto();
        ret.setProcessId(ProcessUtil.convertProcessId(resource.getModelKey()));
        if(isTrigger) {
            ret.setProcessName(StrUtil.format("???????????????{}", resource.getName()));
        } else {
            ret.setProcessName(StrUtil.format("????????????{}", resource.getName()));
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
        List<ConnectorProcessDeployStatusDto> allStatus = CollUtil.newArrayList(triggerDeployStatusMap.values());
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
        ret.setProgressPercentage(NumberUtil.div(100 * successCount, totalCount, 2));
        ret.setTriggerDeployStatusMap(triggerDeployStatusMap);
        ret.setOperateDeployStatusMap(operateDeployStatusMap);
        return ret;
    }

    private Map<String, ConnectorProcessDeployStatusDto> getResourceDeployStatusMap(List<BPMNResource> resourceList, List<Long> envIdList) {

        Map<String, String> processIdModelKeyMap = Opt.ofNullable(resourceList).orElse(ListUtil.empty()).stream()
                .map(BPMNResource::getModelKey).collect(Collectors.toMap(ProcessUtil::convertProcessId, Function.identity()));

        if(CollUtil.isEmpty(processIdModelKeyMap)) return MapUtil.empty();

        Map<String, ConnectorProcessDeployStatusDto> resourceDeployStatusMap = new HashMap<>();

        for (Long envId : Opt.ofNullable(envIdList).orElse(ListUtil.empty())) {
            Map<String, DeployStatusDto> deployStatusDtoMap =
                    processDeployService.fetchDeployStatus(ListUtil.toList(processIdModelKeyMap.keySet()), envId);
            Assert.isTrue(deployStatusDtoMap.size() == resourceList.size(), "???????????????????????????????????????");

            for (Map.Entry<String, DeployStatusDto> entry : deployStatusDtoMap.entrySet()) {
                String processId = entry.getKey();
                DeployStatusDto _deployStatusDto = entry.getValue();
                String _modelKey = processIdModelKeyMap.get(processId);
                ConnectorProcessDeployStatusDto connectorProcessDeployStatusDto =
                        resourceDeployStatusMap.computeIfAbsent(_modelKey,
                                key -> new ConnectorProcessDeployStatusDto(ProcessConst.PROCESS_DEPLOY_STATUS__DEPLOYED, new HashMap<>()));

                if(_deployStatusDto.getStatus() == ProcessConst.PROCESS_DEPLOY_STATUS__EXCEPTION) {
                    // ?????????????????????????????????????????????????????????????????????????????????
                    connectorProcessDeployStatusDto.setStatus(_deployStatusDto.getStatus());

                    // ??????????????????
                    EnvDto envDto = envService.fetchOne(envId);
                    Map<String, String> messageMap =
                            Opt.ofNullable(connectorProcessDeployStatusDto.getErrorMessageMap()).orElse(new LinkedHashMap<>());
                    messageMap.put(envDto.getName(), _deployStatusDto.getMessage());
                    connectorProcessDeployStatusDto.setErrorMessageMap(messageMap);
                } else if (connectorProcessDeployStatusDto.getStatus() == ProcessConst.PROCESS_DEPLOY_STATUS__DEPLOYED) { // ??????????????? 1
                    // ?????????????????????????????????????????????????????????????????????????????????
                    connectorProcessDeployStatusDto.setStatus(_deployStatusDto.getStatus());
                }
            }
        }

        return resourceDeployStatusMap;
    }

}
