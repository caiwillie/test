package com.brandnewdata.mop.poc.process.api;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.api.process.IProcessAPI;
import com.brandnewdata.mop.api.process.dto.MessageDto;
import com.brandnewdata.mop.api.process.dto.SendMessageDto;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class ProcessAPI implements IProcessAPI {

    // todo caiwillie
    // @Resource
    private ZeebeClient zeebeClient;

    @Override
    public Result<MessageDto> sendMessage(SendMessageDto req) {
        String messageName = req.getMessageName();
        Assert.notNull(messageName);

        PublishMessageResponse response = zeebeClient.newPublishMessageCommand()
                .messageName(messageName)
                .correlationKey(req.getCorrelationKey())
                .variables(Optional.ofNullable(req.getVariables()).orElse(MapUtil.empty()))
                .send()
                .join();
        MessageDto resp = toResp(response);
        return Result.OK(resp);
    }


    public MessageDto toResp(PublishMessageResponse response) {
        MessageDto resp = new MessageDto();
        resp.setMessageKey(response.getMessageKey());
        return resp;
    }
}
