package com.brandnewdata.mop.poc.connector.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.api.connector.IConnectorApi;
import com.brandnewdata.mop.api.connector.dto.BPMNResource;
import com.brandnewdata.mop.api.connector.dto.ConnectorResource;
import com.brandnewdata.mop.poc.constant.ProcessConst;
import com.brandnewdata.mop.poc.env.dto.EnvDto;
import com.brandnewdata.mop.poc.env.service.IEnvService;
import com.brandnewdata.mop.poc.process.dto.BpmnXmlDto;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService2;
import com.brandnewdata.mop.poc.process.util.ProcessUtil;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ConnectorApi implements IConnectorApi {

    private final IEnvService envService;

    private final IProcessDeployService2 processDeployService2;

    public ConnectorApi(IEnvService envService,
                        IProcessDeployService2 processDeployService2) {
        this.envService = envService;
        this.processDeployService2 = processDeployService2;
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
                    processDeployService2.releaseDeploy(bpmnXmlDto, envIdList, ProcessConst.PROCESS_BIZ_TYPE__TRIGGER);
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
                    processDeployService2.releaseDeploy(bpmnXmlDto, envIdList, ProcessConst.PROCESS_BIZ_TYPE__OPERATE);
                } catch (Exception e) {
                    throw new RuntimeException(StrUtil.format("【操作】{} 部署异常: {}", operate.getName(), e.getMessage()));
                }
            }
        }

        return Result.OK();
    }

}
