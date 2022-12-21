package com.brandnewdata.mop.poc.bff.controller.homepage;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.bff.service.homepage.HomepageService;
import com.brandnewdata.mop.poc.bff.vo.homepage.ConnectorIndexVo;
import com.brandnewdata.mop.poc.bff.vo.homepage.DataBriefVo;
import com.brandnewdata.mop.poc.bff.vo.homepage.SceneListBriefVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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
//        DataBriefVo datum = new DataBriefVo(1,2,3,4,5,6,7,8);
//        return Result.OK(datum);

        return Result.OK(homepageService.getDataBrief());
    }

    @GetMapping("/list/scene")
    public Result<List<SceneListBriefVo>> sceneInfo() {
//        List<SceneListBriefVo> res = new ArrayList<>();
//        SceneListBriefVo sc = new SceneListBriefVo("这是id","这是场景版本描述","这是场景状态","时间","7日运行总数","7日失败次数");
//        res.add(sc);
//        return Result.OK(res);

        return Result.OK(homepageService.getRemoteSceneInfo());
    }

    @GetMapping("/list/connector")
    public Result<List<ConnectorIndexVo>> connectorInfo(Integer size) {
//        List<ConnectorIndexVo> res = new ArrayList<>();
//        ConnectorIndexVo connectorIndexVo = new ConnectorIndexVo("id","name","v3","4.png","brandData");
//        res.add(connectorIndexVo);
//        return Result.OK(res);
        if(null == size){
            size=8;
        }
        return Result.OK(homepageService.getRemoteConnectorInfo(size));
    }

}
