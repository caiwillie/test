package com.brandnewdata.mop.poc.scene.dto;

import cn.hutool.core.date.LocalDateTimeUtil;
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
        this.setId(po.getId());
        this.setCreateTime(LocalDateTimeUtil.of(po.getCreateTime()));
        this.setUpdateTime(LocalDateTimeUtil.of(po.getUpdateTime()));
        this.setVersionId(po.getVersionId());
        this.setProcessId(po.getProcessId());
        this.setProcessName(po.getProcessName());
        this.setProcessXml(po.getProcessXml());
        this.setProcessImg(po.getProcessImg());
        return this;
    }
}
