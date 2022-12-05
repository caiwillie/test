package com.brandnewdata.mop.poc.scene.service;

import com.brandnewdata.mop.poc.scene.dto.SceneReleaseDeployDto;

import java.util.List;
import java.util.Map;

public interface ISceneReleaseDeployService {

    SceneReleaseDeployDto save(SceneReleaseDeployDto dto);

    List<SceneReleaseDeployDto> fetchByEnvId(Long envId);

    Map<Long, List<SceneReleaseDeployDto>> fetchListByVersionId(List<Long> versionIdList);
}
