package com.brandnewdata.mop.modeler.message;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author caiwillie
 */
@RestController(value = "message")
public class MessageResource {

    @Resource
    private ZeebeClient zeebe;

    @GetMapping(value = "send")
    public void sned(
            @RequestParam String type,
            @RequestParam String correlationKey
    ) {
        PublishMessageResponse response = zeebe.newPublishMessageCommand()
                .messageName(type)
                .correlationKey(correlationKey)
                .send()
                .join();
    }
}
