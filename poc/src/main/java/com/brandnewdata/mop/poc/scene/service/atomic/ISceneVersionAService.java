package com.brandnewdata.mop.poc.scene.service.atomic;

import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;

import java.util.List;
import java.util.Map;

public interface ISceneVersionAService {

    Map<Long, Long> countBySceneId(List<Long> sceneIdList);

    Map<Long, SceneVersionDto> fetchById(List<Long> idList);

    Map<Long, List<SceneVersionDto>> fetchListBySceneId(List<Long> sceneIdList);

    Map<Long, SceneVersionDto> fetchLatestOneBySceneId(List<Long> sceneIdList, List<Integer> statusList);

    List<SceneVersionDto> fetchAll();

    List<SceneVersionDto> fetchAllUnDeployed();

    SceneVersionDto save(SceneVersionDto sceneVersionDto);

    boolean checkNewReleaseVersion(Long sceneId, String version);

    SceneVersionDto fetchOneByIdAndCheckStatus(Long id, int[] statusArr);
}
