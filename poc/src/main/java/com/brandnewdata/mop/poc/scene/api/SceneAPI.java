package com.brandnewdata.mop.poc.scene.api;

import cn.hutool.core.collection.ListUtil;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.api.scene.ISceneAPI;
import com.brandnewdata.mop.api.scene.ListReq;
import com.brandnewdata.mop.api.scene.SceneResp;
import com.brandnewdata.mop.poc.scene.dto.BusinessSceneDTO;
import com.brandnewdata.mop.poc.scene.service.IBusinessSceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SceneAPI implements ISceneAPI {

    @Autowired
    private IBusinessSceneService service;

    @Override
    public Result<List<SceneResp>> listByIds(ListReq req) {
        List<Long> ids = Optional.ofNullable(req).map(ListReq::getIdList).orElse(ListUtil.empty());

        List<BusinessSceneDTO> businessSceneDTOS = service.listByIds(ids);

        List<SceneResp> sceneResps = businessSceneDTOS.stream().map(this::toDTO).collect(Collectors.toList());

        return Result.OK(sceneResps);
    }

    private SceneResp toDTO(BusinessSceneDTO businessSceneDTO) {
        SceneResp sceneResp = new SceneResp();
        sceneResp.setId(businessSceneDTO.getId());
        sceneResp.setName(businessSceneDTO.getName());
        sceneResp.setCreateTime(businessSceneDTO.getCreateTime());
        sceneResp.setUpdateTime(businessSceneDTO.getUpdateTime());
        sceneResp.setImgUrl(businessSceneDTO.getImgUrl());
        return sceneResp;
    }
}
