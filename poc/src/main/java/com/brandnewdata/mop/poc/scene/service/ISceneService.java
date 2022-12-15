package com.brandnewdata.mop.poc.scene.service;

import com.brandnewdata.mop.poc.scene.dto.SceneDto;
import com.brandnewdata.mop.poc.scene.dto.SceneProcessDto;

/**
 * @author caiwillie
 */
public interface ISceneService {

    SceneDto save(SceneDto sceneDTO);

    SceneProcessDto saveProcessDefinition(SceneProcessDto sceneProcessDTO);



}
