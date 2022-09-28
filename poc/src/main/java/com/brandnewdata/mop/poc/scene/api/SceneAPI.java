package com.brandnewdata.mop.poc.scene.api;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.api.scene.ISceneAPI;
import com.brandnewdata.mop.api.scene.ListSceneReq;
import com.brandnewdata.mop.api.scene.SceneResp;
import com.brandnewdata.mop.poc.scene.dto.BusinessSceneDTO;
import com.brandnewdata.mop.poc.scene.service.IBusinessSceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class SceneAPI implements ISceneAPI {

    @Autowired
    private IBusinessSceneService service;

    @Override
    public Result<List<SceneResp>> listByIds(ListSceneReq req) {
        List<Long> ids = Optional.ofNullable(req).map(ListSceneReq::getIdList).orElse(ListUtil.empty());

        List<BusinessSceneDTO> businessSceneDTOS = service.listByIds(ids);

        List<SceneResp> sceneResps = businessSceneDTOS.stream().map(this::toDTO).collect(Collectors.toList());

        return Result.OK(sceneResps);
    }

    private SceneResp toDTO(BusinessSceneDTO businessSceneDTO) {
        SceneResp sceneResp = new SceneResp();
        sceneResp.setId(businessSceneDTO.getId());
        sceneResp.setName(businessSceneDTO.getName());
        sceneResp.setCreateTime(LocalDateTimeUtil.formatNormal(businessSceneDTO.getCreateTime()));
        sceneResp.setUpdateTime(LocalDateTimeUtil.formatNormal(businessSceneDTO.getUpdateTime()));
        sceneResp.setImgUrl(businessSceneDTO.getImgUrl());
        return sceneResp;
    }
}
