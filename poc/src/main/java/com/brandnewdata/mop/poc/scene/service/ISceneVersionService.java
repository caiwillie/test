package com.brandnewdata.mop.poc.scene.service;

import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;

import java.util.List;
import java.util.Map;

public interface ISceneVersionService {

    Map<Long, SceneVersionDto> fetchLatestVersion(List<Long> sceneIdList);

    Map<Long, List<SceneVersionDto>> fetchSceneVersionListBySceneId(List<Long> sceneIdList);

    SceneVersionDto save(SceneVersionDto sceneVersionDto);
}
