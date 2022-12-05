package com.brandnewdata.mop.poc.scene.service;

import com.brandnewdata.mop.poc.scene.dto.SceneDto;
import com.brandnewdata.mop.poc.scene.dto.SceneDto2;
import com.brandnewdata.mop.poc.scene.dto.SceneProcessDto;

import java.util.List;

/**
 * @author caiwillie
 */
public interface ISceneService {


    List<SceneDto> listByIds(List<Long> ids);

    List<SceneDto2> listByIdList(List<Long> idList);

    SceneDto save(SceneDto sceneDTO);

    SceneProcessDto saveProcessDefinition(SceneProcessDto sceneProcessDTO);



}
