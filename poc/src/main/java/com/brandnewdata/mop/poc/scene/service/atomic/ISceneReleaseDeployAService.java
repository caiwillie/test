package com.brandnewdata.mop.poc.scene.service.atomic;

import com.brandnewdata.mop.poc.scene.dto.SceneReleaseDeployDto;

import java.util.List;
import java.util.Map;

public interface ISceneReleaseDeployAService {

    SceneReleaseDeployDto save(SceneReleaseDeployDto dto);

    List<SceneReleaseDeployDto> fetchByEnvId(Long envId);

    Map<Long, List<SceneReleaseDeployDto>> fetchListByVersionId(List<Long> versionIdList);

    void deleteByVersionId(Long versionId);

    void deleteBySceneId(Long sceneId);

    void deleteByVersionIdAndExceptEnvId(Long versionId, List<Long> envIdList);
}
