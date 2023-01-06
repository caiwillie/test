package com.brandnewdata.mop.poc.constant;

public interface SceneConst {


    /**
     * 场景版本状态-配置中
     */
    int SCENE_VERSION_STATUS__CONFIGURING = 1;

    /**
     * 场景版本状态-运行中
     */
    int SCENE_VERSION_STATUS__RUNNING = 2;

    /**
     * 场景版本状态-已停止
     */
    int SCENE_VERSION_STATUS__STOPPED = 3;

    /**
     * 场景版本状态-调试中
     */
    int SCENE_VERSION_STATUS__DEBUGGING = 4;

    /**
     * 场景版本状态-调试部署中
     */
    int SCENE_VERSION_STATUS_DEBUG_DEPLOYING = 5;

    /**
     * 场景版本状态-发布部署中
     */
    int SCENE_VERSION_STATUS_RUN_DEPLOYING = 6;
}
