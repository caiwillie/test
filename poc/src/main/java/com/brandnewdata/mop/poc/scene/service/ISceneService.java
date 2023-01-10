package com.brandnewdata.mop.poc.scene.service;

import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.scene.dto.SceneDto;

import java.util.List;
import java.util.Map;

/**
 * @author caiwillie
 */
public interface ISceneService {

    /**
     * 分页获取场景列表
     * @param pageNum
     * @param pageSize
     * @param name
     * @return
     */
    Page<SceneDto> page(long projectId, int pageNum, int pageSize, String name);



    /**
     * 保存场景
     *
     * @param sceneDto the scene dto
     * @return the scene dto
     */
    SceneDto save(SceneDto sceneDto);

    /**
     * Fetch by id map.
     *
     * @param idList the id list
     * @return the map
     */
    Map<Long, SceneDto> fetchById(List<Long> idList);

    void deleteById(Long id);
}
