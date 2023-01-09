package com.brandnewdata.mop.poc.scene.converter;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Opt;
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
