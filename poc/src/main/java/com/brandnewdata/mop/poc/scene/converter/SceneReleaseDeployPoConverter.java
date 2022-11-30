package com.brandnewdata.mop.poc.scene.converter;

import com.brandnewdata.mop.poc.scene.dto.SceneReleaseDeployDto;
import com.brandnewdata.mop.poc.scene.po.SceneReleaseDeployPo;

public class SceneReleaseDeployPoConverter {

    public static SceneReleaseDeployPo createFrom(SceneReleaseDeployDto dto) {
        SceneReleaseDeployPo po = new SceneReleaseDeployPo();
        po.setId(dto.getId());
        po.setSceneId(dto.getSceneId());
        po.setSceneName(dto.getSceneName());
        po.setVersionId(dto.getVersionId());
        po.setVersionName(dto.getVersionName());
        po.setProcessId(dto.getProcessId());
        po.setProcessName(dto.getProcessName());
        po.setEnvId(dto.getEnvId());
        return po;
    }
}
