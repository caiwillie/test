package com.brandnewdata.mop.api;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.api.dto.ConnectorResource;
import com.brandnewdata.mop.api.dto.StartMessage;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@FeignClient(name = "poc", contextId = "modelApi")
public interface ModelApi {

    /**
     * 发布流程
     *
     * @param resource 连接器的资源
     */
    @RequestMapping("/api/model/deployConnector")
    Result deployConnector(@RequestBody ConnectorResource resource);

    /**
     * 通过消息触发流程
     *
     * @param messages 消息列表
     */
    @RequestMapping("/api/model/startByConnectorMessages")
    Result startByConnectorMessages(@RequestBody List<StartMessage> messages);

    @RequestMapping("/api/model/start/{processId}")
    Result start(@PathVariable("processId") String processId, @RequestBody Map<String, Object> data);

}
