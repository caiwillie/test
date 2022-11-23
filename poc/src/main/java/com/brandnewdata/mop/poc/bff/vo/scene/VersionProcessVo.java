package com.brandnewdata.mop.poc.bff.vo.scene;

import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Getter
@Setter
@Accessors(chain = true)
public class VersionProcessVo {

    private Long id;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long versionId;

    private String processId;

    private String processName;

    private String processXml;

    private String processImg;

    public VersionProcessVo from(VersionProcessDto dto) {
        this.setId(dto.getId());
        this.setCreateTime(dto.getCreateTime());
        this.setUpdateTime(dto.getUpdateTime());
        this.setVersionId(dto.getVersionId());
        this.setProcessId(dto.getProcessId());
        this.setProcessName(dto.getProcessName());
        this.setProcessXml(dto.getProcessXml());
        this.setProcessImg(dto.getProcessImg());
        return this;
    }
}
