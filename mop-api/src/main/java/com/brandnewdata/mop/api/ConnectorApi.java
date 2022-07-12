package com.brandnewdata.mop.api;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.api.dto.TriggerConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(name = "poc", contextId = "connectorApi")
// @RequestMapping("/api/connector")
public interface ConnectorApi {

    /**
     * 获取连接器的监听配置
     *
     * @param connectorId 连接器主键id
     * @return the request param config
     */
    @RequestMapping("/api/connector/getRequestParamConfig")
    Result<List<TriggerConfig>> getRequestParamConfig(Long connectorId);

}
