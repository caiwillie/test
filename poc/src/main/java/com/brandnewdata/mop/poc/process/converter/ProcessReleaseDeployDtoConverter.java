package com.brandnewdata.mop.poc.process.converter;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Opt;
import com.brandnewdata.mop.poc.process.dto.ProcessReleaseDeployDto;
import com.brandnewdata.mop.poc.process.po.ProcessReleaseDeployPo;

public class ProcessReleaseDeployDtoConverter {

    public static ProcessReleaseDeployDto createFrom(ProcessReleaseDeployPo po) {
        ProcessReleaseDeployDto dto = new ProcessReleaseDeployDto();
        dto.setId(po.getId());
        dto.setCreateTime(Opt.ofNullable(po.getCreateTime()).map(LocalDateTimeUtil::of).orElse(null));
        dto.setUpdateTime(Opt.ofNullable(po.getUpdateTime()).map(LocalDateTimeUtil::of).orElse(null));
        dto.setEnvId(po.getEnvId());
        dto.setProcessId(po.getProcessId());
        dto.setProcessZeebeKey(po.getProcessZeebeKey());
        dto.setProcessZeebeVersion(po.getProcessZeebeVersion());
        dto.setProcessZeebeXml(po.getProcessZeebeXml());
        return dto;
    }
}
