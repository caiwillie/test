package com.brandnewdata.mop.poc.scene.api;

import com.brandnewdata.mop.api.scene.ListSceneReq;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SceneAPITest {

    @Autowired
    private SceneAPI sceneAPI;

    @Test
    void listByIds() {
        ListSceneReq listSceneReq = new ListSceneReq();
        sceneAPI.listByIds()

    }
}