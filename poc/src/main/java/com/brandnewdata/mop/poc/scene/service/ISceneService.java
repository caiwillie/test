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
    Page<SceneDto> page(int pageNum, int pageSize, String name);

    SceneDto save(SceneDto sceneDto);

    Map<Long, SceneDto> fetchById(List<Long> idList);

    void deleteById(Long id);
}
