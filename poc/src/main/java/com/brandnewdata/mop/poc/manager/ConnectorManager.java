package com.brandnewdata.mop.poc.manager;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.connector.api.IConnectorBasicInfoFeign;
import com.brandnewdata.connector.api.IConnectorConfFeign;
import com.brandnewdata.mop.poc.error.ErrorMessage;
import com.brandnewdata.mop.poc.manager.dto.TriggerInfo;
import com.brandnewdata.mop.poc.process.dto.TriggerOrOperate;
import com.dxy.library.json.jackson.JacksonUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

        List<TriggerInfo> triggerInfos = Optional
                .ofNullable(JacksonUtil.fromList(info.getConnectorTriggers(), TriggerInfo.class))
                .orElse(ListUtil.empty());

        String triggerId = trigger.getTriggerOrOperateId();
        String xml = null;

        for (TriggerInfo triggerInfo : triggerInfos) {
            String id = triggerInfo.getTriggerName();
            if(StrUtil.equals(triggerId, id)) {
                // 从列表中取出 xml
                xml = triggerInfo.getProcessEditing();
                break;
            }
        }

        return xml;
    }


}
