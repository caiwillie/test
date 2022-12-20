package com.brandnewdata.mop.poc.bff.service.homepage;

import com.brandnewdata.connector.api.IConnectorBasicInfoFeign;
import com.brandnewdata.connector.dto.ConnectorBasicListInfoDTO;
import com.brandnewdata.connector.dto.ConnectorCountDTO;
import com.brandnewdata.mop.poc.bff.converter.homepage.ConnectorIndexVoConverter;
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


    public DataBriefVo getDataBrief() {

        DataBriefVo res = new DataBriefVo();
        try {
            ConnectorCountDTO connectorCountDTO = connectorBasicInfoFeign.getConnectorCount();

            res.setConnectorBaseCount(connectorCountDTO.getBuiltInCount());
            res.setConnectorDevCount(connectorCountDTO.getCustomCount());
        } catch (Exception e) {
            log.error("", e);
            res.setConnectorBaseCount(0);
            res.setConnectorDevCount(0);
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


        return new ArrayList<>();
    }
}
