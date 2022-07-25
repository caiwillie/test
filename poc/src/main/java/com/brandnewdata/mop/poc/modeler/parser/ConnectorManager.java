package com.brandnewdata.mop.poc.modeler.parser;

import com.brandnewdata.connector.api.IConnectorBasicInfoFeign;
import com.brandnewdata.connector.api.IConnectorConfFeign;
import com.brandnewdata.mop.poc.modeler.dto.TriggerOrOperate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ConnectorManager {

    @Resource
    private IConnectorConfFeign confClient;

    @Resource
    private IConnectorBasicInfoFeign basicInfoClient;

    public String getProperties(String id) {
        IConnectorConfFeign.ConnectorConfDTO configInfo = confClient.getConfigInfo(Long.parseLong(id));
        return configInfo.getConfigs();
    }

    public String getTriggerXML(TriggerOrOperate trigger) {
        confClient.getConfigInfo()
    }


}
