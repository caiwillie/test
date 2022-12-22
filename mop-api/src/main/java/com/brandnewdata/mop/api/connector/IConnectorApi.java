package com.brandnewdata.mop.api.connector;


import com.brandnewdata.common.pojo.BasePageResult;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.api.connector.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The interface Connector api.
 */
@FeignClient(name = "poc", contextId = "connectorApi")
public interface IConnectorApi {

    /**
     * 发布流程
     *
     * @param resource 连接器的资源
     * @return the result
     */
    @RequestMapping("/api/connector/deploy")
    Result deploy(@RequestBody ConnectorResource resource);

    /**
     * 调试部署
     *
     * @param resource the resource
     * @return the result
     */
    @RequestMapping("/api/connector/snapshotDeploy")
    Result snapshotDeploy(@RequestBody ConnectorResource resource);

    /**
     * 发布部署
     *
     * @param resource the resource
     * @return the result
     */
    @RequestMapping("/api/connector/releaseDeploy")
    Result releaseDeploy(@RequestBody ConnectorResource resource);

    /**
     * 获取调试部署进度
     *
     * @param resource the resource
     * @return the result
     */
    @RequestMapping("/api/connector/fetchSnapshotDeployProgress")
    Result<ConnectorDeployProgressDto> fetchSnapshotDeployProgress(@RequestBody ConnectorResource resource);

    /**
     * 获取发布部署进度
     *
     * @param resource the resource
     * @return the result
     */
    @RequestMapping("/api/connector/fetchReleaseDeployProgress")
    Result<ConnectorDeployProgressDto> fetchReleaseDeployProgress(@RequestBody ConnectorResource resource);

    /**
     * 获取调试记录
     *
     * @param queryDto the query dto
     * @return the result
     */
    @RequestMapping("/api/connector/fetchSnapshotProcessInstancePage")
    Result<BasePageResult<ProcessInstanceDto>> fetchSnapshotProcessInstancePage(@RequestBody ProcessInstanceQueryDto queryDto);

    /**
     * 获取调试部署的流程定义
     *
     * @param snapshotDeployId the snap deploy id
     */
    @RequestMapping("/api/connector/fetchSnapshotProcessDefinition")
    Result<String> fetchSnapshotProcessDefinition(@RequestParam Long snapshotDeployId);

    /**
     * 触发新的调试
     *
     * @param resource the resource
     */
    @RequestMapping("/api/connector/startSnapshotProcessInstance")
    Result startSnapshotProcessInstance(@RequestBody BPMNResource resource);

}
