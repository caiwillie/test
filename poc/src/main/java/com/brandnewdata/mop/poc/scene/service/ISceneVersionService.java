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

    SceneVersionDto debug(Long id, Long envId);


    Map<Long, Long> countById(List<Long> sceneIdList);

    Map<Long, SceneVersionDto> fetchById(List<Long> id);
}
