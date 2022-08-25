package com.brandnewdata.mop.poc.scene.api;

import cn.hutool.core.collection.ListUtil;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.api.scene.ListSceneReq;
import com.brandnewdata.mop.api.scene.SceneResp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class SceneAPITest {

    @Autowired
    private SceneAPI sceneAPI;

    @Test
    void listByIds() {
        ListSceneReq req = new ListSceneReq();
        req.setIdList(ListUtil.of(12L, 28L));

        Result<List<SceneResp>> result = sceneAPI.listByIds(req);
        return;
    }
}