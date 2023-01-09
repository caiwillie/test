package com.brandnewdata.mop.poc.scene.service.combine;

import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;

import java.util.List;

public interface ISceneVersionCService {

    SceneVersionDto debug(Long id, Long envId);

    SceneVersionDto stopDebug(Long id, Long envId);

    SceneVersionDto stop(Long id);

    SceneVersionDto resume(Long id, List<Long> envIdList);

    SceneVersionDto deploy(Long id, String sceneName, List<Long> envIdList, String version);

    SceneVersionDto copyToNew(Long id);

    void delete(Long id);

    void deleteBySceneId(Long sceneId);
}
