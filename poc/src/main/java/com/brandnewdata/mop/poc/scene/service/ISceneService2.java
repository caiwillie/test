package com.brandnewdata.mop.poc.scene.service;

import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.scene.dto.*;

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

    SceneVersionDto fetchLatestVersion(Long sceneId);

    Map<Long, List<VersionProcessDto>> fetchVersionProcessListByVersionId(List<Long> versionIdList, boolean simple);

    Map<Long, VersionProcessDto> fetchVersionProcessById(List<Long> idList);

    Map<Long, Integer> fetchProcessCountByVersionIdList(List<Long> versionIdList);

    Map<Long, VersionProcessDto> fetchLatestProcessByVersionIdList(List<Long> versionIdList);

}
