package com.brandnewdata.mop.poc.scene.converter;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Opt;
import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;
import com.brandnewdata.mop.poc.scene.po.VersionProcessPo;

public class VersionProcessPoConverter {

    public static VersionProcessPo createFrom(VersionProcessDto dto) {
        VersionProcessPo po = new VersionProcessPo();
        po.setId(dto.getId());
        po.setCreateTime(Opt.ofNullable(dto.getCreateTime()).map(DateUtil::date).orElse(null));
        po.setUpdateTime(Opt.ofNullable(dto.getUpdateTime()).map(DateUtil::date).orElse(null));
        po.setVersionId(dto.getVersionId());
        po.setProcessId(dto.getProcessId());
        po.setProcessName(dto.getProcessName());
        po.setProcessXml(dto.getProcessXml());
        po.setProcessImg(dto.getProcessImg());
        return po;
    }

}
