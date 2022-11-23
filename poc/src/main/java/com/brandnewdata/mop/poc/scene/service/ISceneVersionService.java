package com.brandnewdata.mop.poc.scene.service;

import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;

import java.util.List;

public interface ISceneVersionService {

    SceneVersionDto fetchLatestVersion(Long sceneId);

    List<SceneVersionDto> fetchSceneVersionListBySceneId(Long sceneId);
}
