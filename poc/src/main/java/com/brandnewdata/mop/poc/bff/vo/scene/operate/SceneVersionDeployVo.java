package com.brandnewdata.mop.poc.bff.vo.scene.operate;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SceneVersionDeployVo {

    /**
     * 版本id
     */
    private Long versionId;

    /**
     * 版本名称
     */
    private String versionName;

    /**
     * 创建时间
     */
    private List<VersionProcessDeployVo> processList;
}
