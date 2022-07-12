package com.brandnewdata.mop.api;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.api.dto.BPMNResource;
import com.brandnewdata.mop.api.dto.StartMessage;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(name = "poc", contextId = "modelApi")
@RequestMapping("/api/model")
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
