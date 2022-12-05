package com.brandnewdata.mop.poc.scene.service;

import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;
import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;

import java.util.List;
import java.util.Map;

public interface ISceneVersionService {

    Map<Long, SceneVersionDto> fetchLatestVersion(List<Long> sceneIdList);

    Map<Long, List<SceneVersionDto>> fetchSceneVersionListBySceneId(List<Long> sceneIdList);

    SceneVersionDto save(SceneVersionDto sceneVersionDto);

    VersionProcessDto saveProcess(VersionProcessDto dto);

    void deleteProcess(VersionProcessDto dto);

    void processDebug(VersionProcessDto dto, Map<String, Object> variables);

    SceneVersionDto debug(Long id, Long envId);

    SceneVersionDto stopDebug(Long id, Long envId);

    SceneVersionDto stop(Long id);

    SceneVersionDto resume(Long id, List<Long> envIdList);

    SceneVersionDto deploy(Long id, String sceneName, List<Long> envIdList, String version);

    Map<Long, Long> countById(List<Long> sceneIdList);

    Map<Long, SceneVersionDto> fetchById(List<Long> idList);

    SceneVersionDto copyToNew(Long id);
}
