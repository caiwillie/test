package com.brandnewdata.mop.poc.scene.service;

import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.scene.dto.SceneDto2;

import java.util.List;
import java.util.Map;

/**
 * @author caiwillie
 */
public interface ISceneService2 {

    /**
     * 分页获取场景列表
     * @param pageNum
     * @param pageSize
     * @param name
     * @return
     */
    Page<SceneDto2> page(int pageNum, int pageSize, String name);

    SceneDto2 save(SceneDto2 sceneDto);

    Map<Long, SceneDto2> fetchById(List<Long> idList);
}
