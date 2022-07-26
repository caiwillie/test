package com.brandnewdata.mop.poc.process.parser;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSONObject;
import com.brandnewdata.connector.api.IConnectorBasicInfoFeign;
import com.brandnewdata.connector.api.IConnectorConfFeign;
import com.brandnewdata.mop.poc.error.ErrorMessage;
import com.brandnewdata.mop.poc.process.dto.TriggerOrOperate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

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
        IConnectorBasicInfoFeign.ConnectorBasicInfoDTO info =
                basicInfoClient.getInfoById(trigger.getGroupId(), trigger.getConnectorId(), trigger.getVersion());

        Assert.notNull(info, ErrorMessage.NOT_NULL("触发器"));
        List<JSONObject> triggers = info.getConnectorTriggers();
        return null;
    }


}
