package com.brandnewdata.mop.poc.scene.dto;

import com.brandnewdata.mop.poc.scene.dao.VersionProcessDao;
import com.brandnewdata.mop.poc.scene.po.VersionProcessPo;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class VersionProcessDto {

    private Long id;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long versionId;

    private String processId;

    private String processName;

    private String processXml;

    private String processImg;

    public VersionProcessDto from(VersionProcessPo po) {
        return this;
    }
}
