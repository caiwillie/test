package com.brandnewdata.mop.poc.scene.converter;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Opt;
import com.brandnewdata.mop.poc.scene.dto.SceneDto2;
import com.brandnewdata.mop.poc.scene.po.ScenePo;

public class ScenePoConverter {
    public static ScenePo createFrom(SceneDto2 dto) {
        ScenePo po = new ScenePo();
        po.setId(dto.getId());
        po.setName(dto.getName());
        po.setCreateTime(Opt.ofNullable(dto.getCreateTime()).map(DateUtil::date).orElse(null));
        po.setUpdateTime(Opt.ofNullable(dto.getUpdateTime()).map(DateUtil::date).orElse(null));
        return po;
    }

    public static void updateFrom(SceneDto2 source, ScenePo target) {
        target.setName(source.getName());
    }
}
