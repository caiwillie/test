package com.brandnewdata.mop.poc.scene.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.map.MapUtil;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.api.scene.ISceneApi;
import com.brandnewdata.mop.api.scene.ListSceneReq;
import com.brandnewdata.mop.api.scene.SceneResp;
import com.brandnewdata.mop.api.scene.dto.VersionProcessStartDto;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService2;
import com.brandnewdata.mop.poc.scene.dto.SceneDto;
import com.brandnewdata.mop.poc.scene.dto.SceneReleaseDeployDto;
import com.brandnewdata.mop.poc.scene.service.ISceneService;
import com.dxy.library.json.jackson.JacksonUtil;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class SceneApi implements ISceneApi {

    private final ISceneService service;

    private final IProcessDeployService2 processDeployService;

    public SceneApi(ISceneService service,
                    IProcessDeployService2 processDeployService) {
        this.service = service;
        this.processDeployService = processDeployService;
    }

    @Override
    public Result<List<SceneResp>> listByIds(ListSceneReq req) {
        List<Long> ids = Optional.ofNullable(req).map(ListSceneReq::getIdList).orElse(ListUtil.empty());

        List<SceneDto> sceneDtos = service.listByIds(ids);

        List<SceneResp> sceneResps = sceneDtos.stream().map(this::toDTO).collect(Collectors.toList());

        return Result.OK(sceneResps);
    }

    @Override
    public Result startVersionProcessAsync(List<VersionProcessStartDto> startDtoList) {
        Result ret = Result.OK();
        if(CollUtil.isEmpty(startDtoList)) return ret;

        VersionProcessStartDto versionProcessStartDto = startDtoList.get(0);

        SceneReleaseDeployDto sceneReleaseDeployDto =
                JacksonUtil.from(versionProcessStartDto.getProcessRelevantInfo(), SceneReleaseDeployDto.class);
        Assert.notNull(sceneReleaseDeployDto, "流程相关配置不能为空");

        String processId = sceneReleaseDeployDto.getProcessId();
        Long envId = sceneReleaseDeployDto.getEnvId();
        Map<String, Object> variables = Opt.of(versionProcessStartDto.getVariables()).orElse(MapUtil.empty());

        processDeployService.startAsync(processId, variables, envId);

        return ret;
    }

    private SceneResp toDTO(SceneDto sceneDTO) {
        SceneResp sceneResp = new SceneResp();
        sceneResp.setId(sceneDTO.getId());
        sceneResp.setName(sceneDTO.getName());
        sceneResp.setCreateTime(LocalDateTimeUtil.formatNormal(sceneDTO.getCreateTime()));
        sceneResp.setUpdateTime(LocalDateTimeUtil.formatNormal(sceneDTO.getUpdateTime()));
        sceneResp.setImgUrl(sceneDTO.getImgUrl());
        return sceneResp;
    }
}
