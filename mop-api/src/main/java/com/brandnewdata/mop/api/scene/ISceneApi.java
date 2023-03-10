package com.brandnewdata.mop.api.scene;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.api.scene.dto.SceneDto;
import com.brandnewdata.mop.api.scene.dto.SceneQuery;
import com.brandnewdata.mop.api.scene.dto.VersionProcessStartDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(name = "poc", contextId = "mopScene")
public interface ISceneApi {

    @RequestMapping("/api/scene/listByIds")
    Result<List<SceneDto>> listByIds(@RequestBody SceneQuery req);

    @RequestMapping(value = "/api/scene/version/process/asyncStart")
    Result startVersionProcessAsync(@RequestBody List<VersionProcessStartDto> startDtoList);

    @RequestMapping(value = "/api/scene/version/process/syncStart")
    Result startVersionProcessSync(@RequestBody List<VersionProcessStartDto> startDtoList);
}
