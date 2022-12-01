package com.brandnewdata.mop.poc.scene.converter;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.brandnewdata.mop.poc.scene.dto.SceneDto2;
import com.brandnewdata.mop.poc.scene.po.ScenePo;

public class SceneDtoConverter {

    public static SceneDto2 createFrom(ScenePo po) {
        SceneDto2 dto = new SceneDto2();
        dto.setId(po.getId());
        dto.setName(po.getName());
        dto.setCreateTime(LocalDateTimeUtil.of(po.getCreateTime()));
        dto.setUpdateTime(LocalDateTimeUtil.of(po.getUpdateTime()));
        return dto;
    }

}
