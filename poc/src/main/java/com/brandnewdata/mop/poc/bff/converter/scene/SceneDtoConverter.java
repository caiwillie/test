package com.brandnewdata.mop.poc.bff.converter.scene;

import com.brandnewdata.mop.poc.bff.vo.scene.SceneVo;
import com.brandnewdata.mop.poc.scene.dto.SceneDto;
import com.brandnewdata.mop.poc.scene.dto.SceneDto2;

public class SceneDtoConverter {

    public static SceneDto2 createFrom(SceneVo vo) {
        SceneDto2 dto = new SceneDto2();
        dto.setId(vo.getId());
        dto.setName(vo.getName());
        return dto;
    }
}
