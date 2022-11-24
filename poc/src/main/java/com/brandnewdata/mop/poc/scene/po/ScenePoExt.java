package com.brandnewdata.mop.poc.scene.po;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Opt;
import com.brandnewdata.mop.poc.scene.dto.SceneDto2;

public class ScenePoExt extends ScenePo{
    public ScenePoExt createFrom(SceneDto2 dto) {
        this.setId(dto.getId());
        this.setName(dto.getName());
        this.setCreateTime(Opt.ofNullable(dto.getCreateTime()).map(DateUtil::date).orElse(null));
        this.setUpdateTime(Opt.ofNullable(dto.getUpdateTime()).map(DateUtil::date).orElse(null));
        return this;
    }

    public ScenePoExt updateFrom(SceneDto2 dto) {
        this.setName(dto.getName());
        return this;
    }

    public static ScenePoExt wrapper(ScenePo po) {
        ScenePoExt ret = new ScenePoExt();
        BeanUtil.copyProperties(po, ret);
        return ret;
    }
}
