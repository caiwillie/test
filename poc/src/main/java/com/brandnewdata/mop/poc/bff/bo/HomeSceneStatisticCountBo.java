package com.brandnewdata.mop.poc.bff.bo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HomeSceneStatisticCountBo {

    /**
     * 场景集成数量-总数
     */
    int sceneCount;

    /**
     * 场景集成数量-运行中
     */
    int sceneRunningCount;

    /**
     * 场景7日运行数量-总数
     */
    int processInstanceCount;

    /**
     * 场景7日运行数量-失败数
     */
    int processInstanceFailCount;


}
