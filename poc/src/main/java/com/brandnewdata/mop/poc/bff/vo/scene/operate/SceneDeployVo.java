package com.brandnewdata.mop.poc.bff.vo.scene.operate;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SceneDeployVo {
    /**
     * 场景 id
     */
    private Long sceneId;

    /**
     * 场景名称
     */
    private String sceneName;

    /**
     * 版本列表
     */
    private List<SceneVersionDeployVo> versionList;

}
