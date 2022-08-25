package com.brandnewdata.mop.poc.scene.api;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.api.scene.ISceneAPI;
import com.brandnewdata.mop.api.scene.SceneDTO;
import com.brandnewdata.mop.poc.scene.dto.BusinessSceneDTO;
import com.brandnewdata.mop.poc.scene.service.IBusinessSceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SceneAPI implements ISceneAPI {

    @Autowired
    private IBusinessSceneService service;

    @Override
    public Result<List<SceneDTO>> listByIds(List<Long> ids) {
        List<BusinessSceneDTO> businessSceneDTOS = service.listByIds(ids);

        List<SceneDTO> sceneDTOS = businessSceneDTOS.stream().map(this::toDTO).collect(Collectors.toList());

        return Result.OK(sceneDTOS);
    }

    private SceneDTO toDTO(BusinessSceneDTO businessSceneDTO) {
        SceneDTO sceneDTO = new SceneDTO();
        sceneDTO.setId(businessSceneDTO.getId());
        sceneDTO.setName(businessSceneDTO.getName());
        sceneDTO.setCreateTime(businessSceneDTO.getCreateTime());
        sceneDTO.setUpdateTime(businessSceneDTO.getUpdateTime());
        sceneDTO.setImgUrl(businessSceneDTO.getImgUrl());
        return sceneDTO;
    }
}
