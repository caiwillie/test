package com.brandnewdata.mop.poc.bff.converter.scene;

import com.brandnewdata.mop.poc.bff.vo.scene.VersionProcessVo;
import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;

public class VersionProcessDtoConverter {

    public static VersionProcessDto createFrom(VersionProcessVo vo) {
        VersionProcessDto dto = new VersionProcessDto();
        dto.setId(vo.getId());
        dto.setCreateTime(vo.getCreateTime());
        dto.setUpdateTime(vo.getUpdateTime());
        dto.setVersionId(vo.getVersionId());
        dto.setProcessId(vo.getProcessId());
        dto.setProcessName(vo.getProcessName());
        dto.setProcessXml(vo.getProcessXml());
        dto.setProcessImg(vo.getProcessImg());
        return dto;
    }
}
