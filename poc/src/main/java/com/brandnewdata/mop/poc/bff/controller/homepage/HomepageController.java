package com.brandnewdata.mop.poc.bff.controller.homepage;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.bff.service.homepage.HomepageService;
import com.brandnewdata.mop.poc.bff.vo.homepage.DataBriefVo;
import com.brandnewdata.mop.poc.bff.vo.homepage.SceneListBriefVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 首页统计信息相关接口
 *
 * @author jekyll 2022-12-13 14:39
 */
@RestController
@RequestMapping("/index")
public class HomepageController {

    @Resource
    HomepageService homepageService;

    @GetMapping("/statistic/info")
    public Result<DataBriefVo> dataInfo() {
        DataBriefVo datum = new DataBriefVo(1,2,3,4,5,6,7,8);
        return Result.OK(datum);
    }

    @GetMapping("/list/scene")
    public Result<List<SceneListBriefVo>> sceneInfo() {
        List<SceneListBriefVo> res = new ArrayList<>();
        return Result.OK(res);
    }

    @GetMapping("/list/connector")
    public Result<List<SceneListBriefVo>> connectorInfo() {
        List<SceneListBriefVo> res = new ArrayList<>();
        return Result.OK(res);
    }

}
