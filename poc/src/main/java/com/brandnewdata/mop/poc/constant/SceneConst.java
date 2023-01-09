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

    int SCENE_DEPLOY_STATUS_SNAPSHOT_UNDEPLOY = 3;

    int SCENE_DEPLOY_STATUS_RELEASE_UNDEPLOY = 4;

    int SCENE_DEPLOY_STATUS__DEPLOYED = 1;

    int SCENE_DEPLOY_STATUS__EXCEPTION = 2;
}
