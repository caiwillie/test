package com.brandnewdata.mop.poc.scene.api;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.api.scene.ISceneAPI;
import com.brandnewdata.mop.api.scene.SceneDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SceneAPI implements ISceneAPI {


    @Override
    public Result<List<SceneDTO>> listByIds(List<Long> ids) {

        return null;
    }
}
