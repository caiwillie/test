package com.brandnewdata.mop.poc.bff.converter.scene;

import com.brandnewdata.mop.poc.bff.vo.scene.SceneVersionVo;
import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;

public class SceneVersionVoConverter {

    public static SceneVersionVo createFrom(SceneVersionDto dto) {
        SceneVersionVo vo = new SceneVersionVo();
        vo.setId(dto.getId());
        vo.setCreateTime(dto.getCreateTime());
        vo.setUpdateTime(dto.getUpdateTime());
        vo.setVersion(dto.getVersion());
        vo.setSceneId(dto.getSceneId());
        vo.setStatus(dto.getStatus());
        return vo;
    }
}
