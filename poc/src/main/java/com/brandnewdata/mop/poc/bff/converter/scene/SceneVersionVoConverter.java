package com.brandnewdata.mop.poc.bff.converter.scene;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.NumberUtil;
import com.brandnewdata.mop.poc.bff.converter.env.EnvVoConverter;
import com.brandnewdata.mop.poc.bff.vo.env.EnvVo;
import com.brandnewdata.mop.poc.bff.vo.scene.SceneVersionVo;
import com.brandnewdata.mop.poc.constant.SceneConst;
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
        // 根据deploy status 进行特殊处理
        Integer deployStatus = Opt.ofNullable(dto.getDeployStatus()).orElse(SceneConst.SCENE_DEPLOY_STATUS__DEPLOYED);
        if(NumberUtil.equals(deployStatus, SceneConst.SCENE_DEPLOY_STATUS_SNAPSHOT_UNDEPLOY)) {
            vo.setDeployStatus(0);
            vo.setStatus(5);
        } else if (NumberUtil.equals(deployStatus, SceneConst.SCENE_DEPLOY_STATUS_RELEASE_UNDEPLOY)) {
            vo.setDeployStatus(0);
            vo.setStatus(6);
        } else {
            vo.setDeployStatus(deployStatus);
            vo.setStatus(dto.getStatus());
        }
        vo.setDeployProgressPercentage(dto.getDeployProgressPercentage());
        vo.setExceptionMessage(dto.getExceptionMessage());
        return vo;
    }

    public static SceneVersionVo createFrom(SceneVersionDto dto, List<EnvDto> envDtoList) {
        SceneVersionVo vo = createFrom(dto);

        List<EnvVo> envVoList = Opt.ofNullable(envDtoList).orElse(ListUtil.empty()).stream()
                .map(EnvVoConverter::createFrom).collect(Collectors.toList());

        vo.setEnvList(envVoList);
        return vo;
    }
}
