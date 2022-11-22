package com.brandnewdata.mop.poc.scene.service;

import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.scene.dto.SceneDto;
import com.brandnewdata.mop.poc.scene.dto.SceneDto2;
import com.brandnewdata.mop.poc.scene.dto.SceneProcessDto;

import java.util.List;

/**
 * @author caiwillie
 */
public interface ISceneService {

    Page<SceneDto> page(int pageNumber, int pageSize, String name);

    SceneDto getOne(Long id);

    List<SceneDto> listByIds(List<Long> ids);

    List<SceneDto2> listByIdList(List<Long> idList);

    SceneDto save(SceneDto sceneDTO);

    SceneProcessDto saveProcessDefinition(SceneProcessDto sceneProcessDTO);

    void deploy(SceneProcessDto sceneProcessDTO);

    void deleteProcessDefinition(SceneProcessDto sceneProcessDTO);

    void delete(SceneDto sceneDTO);

}
