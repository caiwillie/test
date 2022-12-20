package com.brandnewdata.mop.api.connector;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.api.connector.dto.ConnectorResource;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "poc", contextId = "connectorApi")
public interface IConnectorApi {

    /**
     * 发布流程
     *
     * @param resource 连接器的资源
     */
    @RequestMapping("/api/connector/deploy")
    Result deploy(@RequestBody ConnectorResource resource);

    @RequestMapping("/api/connector/snapshotDeploy")
    Result snapshotDeploy(@RequestBody ConnectorResource resource);

    @RequestMapping("/api/connector/releaseDeploy")
    Result releaseDeploy(@RequestBody ConnectorResource resource);

}
