package com.brandnewdata.mop.poc.api;

import com.brandnewdata.mop.poc.common.service.result.Result;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * The interface Model api.
 *
 * @author caiwillie
 */
@FeignClient(name = "poc", contextId = "modelApi")
public interface ModelApi {

    /**
     * 流程资源实体
     */
    @Data
    public static class BPMNResource {
        /**
         * 模型标识 (需要唯一)
         */
        private String modelKey;

        /**
         * 模型名称
         *
         * @param bpmnList the bpmn list
         */
        private String name;

        /**
         * 流程XML
         *
         * @param bpmnList the bpmn list
         */
        private String editorXML;
    }

    /**
     * 发布流程
     *
     * @param bpmnList the bpmn list
     */
    @RequestMapping("/deploy")
    Result deploy(@RequestBody List<BPMNResource> bpmnList);

}