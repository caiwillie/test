package com.brandnewdata.mop.poc.scene.converter;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;
import com.brandnewdata.mop.poc.scene.po.SceneVersionPo;

public class SceneVersionDtoConverter {

    public static SceneVersionDto createFrom(SceneVersionPo po) {
        SceneVersionDto dto = new SceneVersionDto();
        dto.setId(po.getId());
        dto.setCreateTime(LocalDateTimeUtil.of(po.getCreateTime()));
        dto.setUpdateTime(LocalDateTimeUtil.of(po.getUpdateTime()));
        dto.setVersion(po.getVersion());
        dto.setSceneId(po.getSceneId());
        dto.setStatus(po.getStatus());
        dto.setDeployProgressPercentage(po.getDeployProgressPercentage());
        dto.setExceptionMessage(po.getExceptionMessage());
        dto.setDeployStatus(po.getDeployStatus());
        return dto;
    }
}
