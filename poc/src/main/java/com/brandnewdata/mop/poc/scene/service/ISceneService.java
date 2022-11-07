package com.brandnewdata.mop.poc.scene.service;

import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.scene.dto.SceneDTO;
import com.brandnewdata.mop.poc.scene.dto.SceneProcessDTO;

import java.util.List;

/**
 * @author caiwillie
 */
public interface ISceneService {

    Page<SceneDTO> page(int pageNumber, int pageSize, String name);

    SceneDTO getOne(Long id);

    List<SceneDTO> listByIds(List<Long> ids);

    SceneDTO save(SceneDTO sceneDTO);

    SceneProcessDTO saveProcessDefinition(SceneProcessDTO sceneProcessDTO);

    void deploy(SceneProcessDTO sceneProcessDTO);

    void deleteProcessDefinition(SceneProcessDTO sceneProcessDTO);

    void delete(SceneDTO sceneDTO);

}
