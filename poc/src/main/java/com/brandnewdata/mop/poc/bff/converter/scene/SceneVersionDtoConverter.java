package com.brandnewdata.mop.poc.bff.converter.scene;

import com.brandnewdata.mop.poc.bff.vo.scene.SceneVersionVo;
import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;

public class SceneVersionDtoConverter {

    public static SceneVersionDto create(SceneVersionVo vo) {
        SceneVersionDto dto = new SceneVersionDto();
        dto.setId(vo.getId());
        dto.setVersion(vo.getVersion());
        dto.setSceneId(vo.getSceneId());
        dto.setStatus(vo.getStatus());
        return dto;
    }
}
