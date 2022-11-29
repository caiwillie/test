package com.brandnewdata.mop.poc.bff.converter.scene;

import com.brandnewdata.mop.poc.bff.vo.scene.VersionProcessVo;
import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;

public class VersionProcessVoConverter {

    public static VersionProcessVo createFrom(VersionProcessDto dto) {
        VersionProcessVo vo = new VersionProcessVo();
        vo.setId(dto.getId());
        vo.setCreateTime(dto.getCreateTime());
        vo.setUpdateTime(dto.getUpdateTime());
        vo.setVersionId(dto.getVersionId());
        vo.setProcessId(dto.getProcessId());
        vo.setProcessName(dto.getProcessName());
        vo.setProcessXml(dto.getProcessXml());
        vo.setProcessImg(dto.getProcessImg());
        return vo;
    }
}
