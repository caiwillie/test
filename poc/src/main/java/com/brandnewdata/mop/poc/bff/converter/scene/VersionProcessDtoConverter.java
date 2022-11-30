package com.brandnewdata.mop.poc.bff.converter.scene;

import cn.hutool.core.lang.Opt;
import com.brandnewdata.mop.poc.bff.vo.scene.VersionProcessVo;
import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;

public class VersionProcessDtoConverter {

    public static VersionProcessDto createFrom(VersionProcessVo vo) {
        VersionProcessDto dto = new VersionProcessDto();
        dto.setId(Opt.ofNullable(vo.getId()).map(Long::parseLong).orElse(null));
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
