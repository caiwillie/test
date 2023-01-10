package com.brandnewdata.mop.poc.scene.converter;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.brandnewdata.mop.poc.scene.dto.SceneDto;
import com.brandnewdata.mop.poc.scene.po.ScenePo;

public class SceneDtoConverter {

    public static SceneDto createFrom(ScenePo po) {
        SceneDto dto = new SceneDto();
        dto.setId(po.getId());
        dto.setName(po.getName());
        dto.setProjectId(po.getProjectId());
        dto.setCreateTime(LocalDateTimeUtil.of(po.getCreateTime()));
        dto.setUpdateTime(LocalDateTimeUtil.of(po.getUpdateTime()));
        return dto;
    }

}
