package com.brandnewdata.mop.poc.scene.converter;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;
import com.brandnewdata.mop.poc.scene.po.SceneVersionPo;

public class SceneVersionPoConverter extends SceneVersionPo {

    public static SceneVersionPo createFrom(SceneVersionDto dto) {
        Assert.notNull(dto.getSceneId(), "场景id不能为空");
        Assert.notNull(dto.getVersion(), "版本不能为空");
        Assert.notNull(dto.getStatus(), "状态不能为空");

        SceneVersionPo po = new SceneVersionPo();
        po.setId(dto.getId());
        po.setCreateTime(Opt.ofNullable(dto.getCreateTime()).map(DateUtil::date).orElse(null));
        po.setUpdateTime(Opt.ofNullable(dto.getUpdateTime()).map(DateUtil::date).orElse(null));
        po.setSceneId(dto.getSceneId());
        po.setVersion(dto.getVersion());
        po.setStatus(dto.getStatus());
        return po;
    }
}
