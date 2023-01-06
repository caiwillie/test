package com.brandnewdata.mop.poc.bff.converter.scene;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Opt;
import com.brandnewdata.mop.poc.bff.converter.env.EnvVoConverter;
import com.brandnewdata.mop.poc.bff.vo.env.EnvVo;
import com.brandnewdata.mop.poc.bff.vo.scene.SceneVersionVo;
import com.brandnewdata.mop.poc.env.dto.EnvDto;
import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;

import java.util.List;
import java.util.stream.Collectors;

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

    public static SceneVersionVo createFrom(SceneVersionDto dto, List<EnvDto> envDtoList) {
        SceneVersionVo vo = new SceneVersionVo();
        vo.setId(dto.getId());
        vo.setCreateTime(dto.getCreateTime());
        vo.setUpdateTime(dto.getUpdateTime());
        vo.setVersion(dto.getVersion());
        vo.setSceneId(dto.getSceneId());
        vo.setStatus(dto.getStatus());
        vo.setExceptionMessage(dto.getExceptionMessage());

        List<EnvVo> envVoList = Opt.ofNullable(envDtoList).orElse(ListUtil.empty()).stream()
                .map(EnvVoConverter::createFrom).collect(Collectors.toList());

        vo.setEnvList(envVoList);
        return vo;
    }
}
