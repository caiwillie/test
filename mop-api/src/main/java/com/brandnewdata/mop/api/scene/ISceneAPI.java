package com.brandnewdata.mop.api.scene;

import com.brandnewdata.common.webresult.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(name = "poc", contextId = "mop_scene")
public interface ISceneAPI {

    @RequestMapping("/api/scene/listByIds")
    Result<List<SceneDTO>> listByIds(@RequestBody List<Long> ids);

}
