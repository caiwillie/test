package com.brandnewdata.mop.poc.scene.converter;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Opt;
import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;
import com.brandnewdata.mop.poc.scene.po.SceneVersionPo;

public class SceneVersionPoConverter extends SceneVersionPo {

    public SceneVersionPo createFrom(SceneVersionDto dto) {
        this.setId(dto.getId());
        this.setCreateTime(Opt.ofNullable(dto.getCreateTime()).map(DateUtil::date).orElse(null));
        this.setUpdateTime(Opt.ofNullable(dto.getUpdateTime()).map(DateUtil::date).orElse(null));
        this.setVersion(dto.getVersion());
        this.setStatus(dto.getStatus());
        return this;
    }
}
