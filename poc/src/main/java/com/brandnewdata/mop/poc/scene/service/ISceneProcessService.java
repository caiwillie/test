package com.brandnewdata.mop.poc.scene.service;

import com.brandnewdata.mop.poc.scene.dto.SceneProcessDto;

import java.util.List;

public interface ISceneProcessService {

    List<SceneProcessDto> listByProcessIdList(List<String> processIdList);

    List<SceneProcessDto> listBySceneIdList(List<Long> sceneIdList);
}
