package com.brandnewdata.mop.poc.scene.converter;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;
import com.brandnewdata.mop.poc.scene.po.VersionProcessPo;

public class VersionProcessDtoConverter {

    public static VersionProcessDto from(VersionProcessPo po) {
        VersionProcessDto dto = new VersionProcessDto();
        dto.setId(po.getId());
        dto.setCreateTime(LocalDateTimeUtil.of(po.getCreateTime()));
        dto.setUpdateTime(LocalDateTimeUtil.of(po.getUpdateTime()));
        dto.setVersionId(po.getVersionId());
        dto.setProcessId(po.getProcessId());
        dto.setProcessName(po.getProcessName());
        dto.setProcessXml(po.getProcessXml());
        dto.setProcessImg(po.getProcessImg());
        return dto;
    }
}
