package com.brandnewdata.mop.api;

import com.brandnewdata.common.webresult.Result;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(name = "mop", contextId = "modelApi")
public interface ModelApi {

    /**
     * 发布流程
     *
     * @param bpmnList the bpmn list
     */
    @RequestMapping("/deploy")
    Result deploy(@RequestBody List<BPMNResource> bpmnList);

    /**
     * 通过消息触发流程
     */
    @RequestMapping("/startByMessages")
    Result startByMessages(@RequestBody List<StartMessage> messages);

}
