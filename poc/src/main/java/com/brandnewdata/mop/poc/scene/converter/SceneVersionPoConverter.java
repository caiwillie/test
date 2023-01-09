package com.brandnewdata.mop.poc.scene.converter;

import cn.hutool.core.lang.Assert;
import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;
import com.brandnewdata.mop.poc.scene.po.SceneVersionPo;

public class SceneVersionPoConverter extends SceneVersionPo {

    public static SceneVersionPo createFrom(SceneVersionDto dto) {
        Assert.notNull(dto.getSceneId(), "场景id不能为空");
        Assert.notNull(dto.getVersion(), "版本不能为空");
        Assert.notNull(dto.getStatus(), "状态不能为空");

        SceneVersionPo po = new SceneVersionPo();
        po.setId(dto.getId());
        po.setSceneId(dto.getSceneId());
        po.setVersion(dto.getVersion());
        po.setStatus(dto.getStatus());
        po.setDeployProgressPercentage(dto.getDeployProgressPercentage());
        po.setExceptionMessage(dto.getExceptionMessage());
        po.setDeployStatus(dto.getDeployStatus());
        return po;
    }

}
