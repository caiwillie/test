package com.brandnewdata.mop.poc.bff.service.homepage;

import cn.hutool.core.collection.CollUtil;
import com.brandnewdata.connector.api.IConnectorBasicInfoFeign;
import com.brandnewdata.connector.dto.ConnectorBasicListInfoDTO;
import com.brandnewdata.connector.dto.ConnectorCountDTO;
import com.brandnewdata.mop.poc.bff.bo.HomeApiStatisticCountBo;
import com.brandnewdata.mop.poc.bff.bo.HomeSceneBo;
import com.brandnewdata.mop.poc.bff.bo.HomeSceneStatisticCountBo;
import com.brandnewdata.mop.poc.bff.converter.homepage.ConnectorIndexVoConverter;
import com.brandnewdata.mop.poc.bff.converter.homepage.SceneIndexVoConverter;
import com.brandnewdata.mop.poc.bff.vo.homepage.ConnectorIndexVo;
import com.brandnewdata.mop.poc.bff.vo.homepage.DataBriefVo;
import com.brandnewdata.mop.poc.bff.vo.homepage.SceneListBriefVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 首页接口服务
 *
 * @author jekyll 2022-12-15 14:22
 */
@Slf4j
@Service
public class HomepageService {

    @Resource
    IConnectorBasicInfoFeign connectorBasicInfoFeign;

    private final HomeSceneStatisticService homeSceneStatisticService;

    private final HomeApiStatisticService homeApiStatisticService;

    public HomepageService(HomeSceneStatisticService homeSceneStatisticService,
                           HomeApiStatisticService homeApiStatisticService) {
        this.homeSceneStatisticService = homeSceneStatisticService;
        this.homeApiStatisticService = homeApiStatisticService;
    }

    public DataBriefVo getDataBrief() {

        DataBriefVo res = new DataBriefVo();
        try {
            ConnectorCountDTO connectorCountDTO = connectorBasicInfoFeign.getConnectorCount();

            res.setConnectorBaseCount(connectorCountDTO.getBuiltInCount());
            res.setConnectorDevCount(connectorCountDTO.getCustomCount());
        } catch (Exception e) {
            log.error("", e);
            throw e;
//            res.setConnectorBaseCount(0);
//            res.setConnectorDevCount(0);
        }

        try{
            HomeSceneStatisticCountBo homeSceneStatisticCountBo = homeSceneStatisticService.statisticCount();

            if(homeSceneStatisticCountBo != null){
                res.setSceneInProgress(homeSceneStatisticCountBo.getSceneRunningCount());
                res.setSceneTotal(homeSceneStatisticCountBo.getSceneCount());
                res.setWeeklyRuntimeTotal(homeSceneStatisticCountBo.getProcessInstanceCount());
                res.setWeeklyRuntimeFail(homeSceneStatisticCountBo.getProcessInstanceFailCount());
            }

            HomeApiStatisticCountBo homeApiStatisticCountBo = homeApiStatisticService.statisticCount();
            if(homeApiStatisticCountBo != null) {
                res.setApiServiceCount(homeApiStatisticCountBo.getApiCount());
                res.setApiPathCount(homeApiStatisticCountBo.getApiPathCount());
            }

        }catch (Exception e2) {
            log.error("",e2);
            throw e2;
        }


        return res;
    }

    public List<ConnectorIndexVo> getRemoteConnectorInfo(Integer size) {
        List<ConnectorIndexVo> res = new ArrayList<>();
        try{
            List<ConnectorBasicListInfoDTO> resFeign =  connectorBasicInfoFeign.getInfoList(size);

            resFeign.forEach(data->{
                res.add(ConnectorIndexVoConverter.createForm(data));
            });
        }catch ( Exception e){
            log.error("", e);
        }
        return res;
    }

    public List<SceneListBriefVo> getRemoteSceneInfo() {
        List<SceneListBriefVo> res = new ArrayList<>();


        List<HomeSceneBo> homeSceneBoList = homeSceneStatisticService.sceneList();

        if(CollUtil.isNotEmpty(homeSceneBoList)){
            homeSceneBoList.forEach(data->{
                res.add(SceneIndexVoConverter.createForm(data));
            });
        }

        return res;
    }

}
