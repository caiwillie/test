package com.brandnewdata.mop.poc.operate.rest;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.operate.dto.FlowNodeInstanceDto;
import com.brandnewdata.mop.poc.operate.dto.FlowNodeInstanceRequest;
import com.brandnewdata.mop.poc.operate.service.FlowNodeInstanceService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 运行监控相关的接口
 */
@RestController
public class FlowNodeInstanceRest {

    @Autowired
    private FlowNodeInstanceService service;

    /**
     * 获取流程节点列表
     *
     * @param processInstanceId 流程实例id
     * @return the result
     */
    @GetMapping("/rest/operate/flowNodeInstance/list")
    public Result<List<FlowNodeInstanceDto>> list(@RequestParam String processInstanceId) {
        List<FlowNodeInstanceDto> list = service.list(processInstanceId);
        return Result.OK(list);
    }


    @GetMapping("/rest/operate/flowNodeInstance/metadata")
    public Result metadata(@RequestParam String processInstanceId, @RequestParam String flowNodeInstanceId) {
        return null;
    }

}
