package com.brandnewdata.mop.poc.scene.service;

import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;
import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;

import java.util.List;
import java.util.Map;

public interface ISceneVersionService {

    /*VersionProcessDto saveProcess(VersionProcessDto dto);

    void deleteProcess(VersionProcessDto dto);*/

    void processDebug(VersionProcessDto dto, Map<String, Object> variables);

    SceneVersionDto debug(Long id, Long envId);

    SceneVersionDto stopDebug(Long id, Long envId);

    SceneVersionDto stop(Long id);

    SceneVersionDto resume(Long id, List<Long> envIdList);

    SceneVersionDto deploy(Long id, String sceneName, List<Long> envIdList, String version);

    SceneVersionDto copyToNew(Long id);

    /*SceneVersionDto fetchOneByIdAndCheckStatus(Long id, int[] statusArr);*/
}
