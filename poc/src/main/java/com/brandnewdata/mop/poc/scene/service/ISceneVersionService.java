package com.brandnewdata.mop.poc.scene.service;

import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;
import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;

import java.util.List;
import java.util.Map;

public interface ISceneVersionService {

    Map<Long, SceneVersionDto> fetchLatestVersion(List<Long> sceneIdList);

    Map<Long, List<SceneVersionDto>> fetchSceneVersionListBySceneId(List<Long> sceneIdList);

    SceneVersionDto save(SceneVersionDto sceneVersionDto);

    VersionProcessDto processSave(VersionProcessDto dto);

    void processDelete(VersionProcessDto dto);

    SceneVersionDto debug(SceneVersionDto sceneVersionDto);
    Map<Long, Long> countById(List<Long> sceneIdList);

    Map<Long, SceneVersionDto> fetchById(List<Long> id);
}
