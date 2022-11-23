package com.brandnewdata.mop.poc.bff.controller.scene;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.bff.service.scene.SceneBffService;
import com.brandnewdata.mop.poc.bff.vo.scene.SceneVo;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.scene.dto.SceneDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SceneController2 {

    private final SceneBffService sceneBffService;

    public SceneController2(SceneBffService sceneBffService) {
        this.sceneBffService = sceneBffService;
    }

    /**
     * 分页列表
     *
     * @param projectId 项目id
     * @param pageNum   分页码
     * @param pageSize  分页大小
     * @param name      名称（模糊搜索）
     * @return the result
     */
    @GetMapping(value = "/rest/scene/page")
    public Result<Page<SceneVo>> page(
            @RequestParam(required = false) String projectId,
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String name) {
        Page<SceneVo> page = sceneBffService.page(pageNum, pageSize, name);
        return Result.OK(page);
    }


}
