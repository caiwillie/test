package com.brandnewdata.mop.poc.scene.converter;

import com.brandnewdata.mop.poc.scene.dto.SceneDto;
import com.brandnewdata.mop.poc.scene.po.ScenePo;

public class ScenePoConverter {
    public static ScenePo createFrom(SceneDto dto) {
        ScenePo po = new ScenePo();
        po.setId(dto.getId());
        po.setName(dto.getName());
        po.setProjectId(dto.getProjectId());
        return po;
    }

    public static void updateFrom(SceneDto source, ScenePo target) {
        target.setName(source.getName());
    }
}
