package com.brandnewdata.mop.poc.connector.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.api.connector.IConnectorApi;
import com.brandnewdata.mop.api.connector.dto.BPMNResource;
import com.brandnewdata.mop.api.connector.dto.ConnectorResource;
import com.brandnewdata.mop.poc.constant.ProcessConst;
import com.brandnewdata.mop.poc.env.dto.EnvDto;
import com.brandnewdata.mop.poc.env.service.IEnvService;
import com.brandnewdata.mop.poc.process.dto.BizDeployDto;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService2;
import com.brandnewdata.mop.poc.process.util.ProcessUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
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
        Assert.notEmpty(envDtoList, "环境列表为空");
        List<Long> envIdList = envDtoList.stream().map(EnvDto::getId).collect(Collectors.toList());

        // 部署触发器
        if(CollUtil.isNotEmpty(triggers)) {
            for (BPMNResource trigger : triggers) {
                BizDeployDto bizDeployDto = new BizDeployDto();
                bizDeployDto.setProcessId(ProcessUtil.convertProcessId(trigger.getModelKey()));
                bizDeployDto.setProcessName(StrUtil.format("【触发器】{}", trigger.getName()));
                bizDeployDto.setProcessXml(trigger.getEditorXML());
                processDeployService2.releaseDeploy(bizDeployDto, envIdList, ProcessConst.PROCESS_BIZ_TYPE__TRIGGER);
            }
        }

        // 部署操作
        if(CollUtil.isNotEmpty(operates)) {
            for (BPMNResource operate : operates) {
                BizDeployDto bizDeployDto = new BizDeployDto();
                bizDeployDto.setProcessId(ProcessUtil.convertProcessId(operate.getModelKey()));
                bizDeployDto.setProcessName(StrUtil.format("【操作】{}", operate.getName()));
                bizDeployDto.setProcessXml(operate.getEditorXML());
                processDeployService2.releaseDeploy(bizDeployDto, envIdList, ProcessConst.PROCESS_BIZ_TYPE__OPERATE);
            }
        }

        return Result.OK();
    }

}