package com.brandnewdata.mop.poc.bff.converter.scene;

import cn.hutool.core.lang.Opt;
import com.brandnewdata.mop.poc.bff.vo.scene.SceneVo;
import com.brandnewdata.mop.poc.scene.dto.SceneDto;

public class SceneDtoConverter {

    public static SceneDto createFrom(SceneVo vo) {
        SceneDto dto = new SceneDto();
        dto.setId(vo.getId());
        dto.setName(vo.getName());
        dto.setProjectId(Opt.ofNullable(vo.getProjectId()).map(Long::valueOf).orElse(null));
        return dto;
    }
}
