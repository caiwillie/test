package com.brandnewdata.mop.poc.scene.converter;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Opt;
import com.brandnewdata.mop.poc.scene.dto.SceneReleaseDeployDto;
import com.brandnewdata.mop.poc.scene.po.SceneReleaseDeployPo;

public class SceneReleaseDeployDtoConverter {

    /*
private Long sceneId;

    private String sceneName;

    private Long versionId;

    private String versionName;

    private String processId;

    private String processName;

    private Long envId;
    * */
    public static SceneReleaseDeployDto createFrom(SceneReleaseDeployPo po) {
        SceneReleaseDeployDto dto = new SceneReleaseDeployDto();
        dto.setId(po.getId());
        dto.setCreateTime(Opt.ofNullable(po.getCreateTime()).map(LocalDateTimeUtil::of).orElse(null));
        dto.setUpdateTime(Opt.ofNullable(po.getUpdateTime()).map(LocalDateTimeUtil::of).orElse(null));
        dto.setSceneId(po.getSceneId());
        dto.setSceneName(po.getSceneName());
        dto.setVersionId(po.getVersionId());
        dto.setVersionName(po.getVersionName());
        dto.setProcessId(po.getProcessId());
        dto.setProcessName(po.getProcessName());
        dto.setEnvId(po.getEnvId());
        return dto;
    }
}
