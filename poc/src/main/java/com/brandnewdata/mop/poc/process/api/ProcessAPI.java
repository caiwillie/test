package com.brandnewdata.mop.poc.process.api;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.api.process.IProcessAPI;
import com.brandnewdata.mop.api.process.SendMessageReq;
import com.brandnewdata.mop.api.process.SendMessageResp;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Optional;

@RestController
public class ProcessAPI implements IProcessAPI {

    // @Resource
    private ZeebeClient zeebeClient;

    @Override
    public Result<SendMessageResp> sendMessage(SendMessageReq req) {
        String messageName = req.getMessageName();
        Assert.notNull(messageName);

        PublishMessageResponse response = zeebeClient.newPublishMessageCommand()
                .messageName(messageName)
                .correlationKey(req.getCorrelationKey())
                .variables(Optional.ofNullable(req.getVariables()).orElse(MapUtil.empty()))
                .send()
                .join();
        SendMessageResp resp = toResp(response);
        return Result.OK(resp);
    }


    public SendMessageResp toResp(PublishMessageResponse response) {
        SendMessageResp resp = new SendMessageResp();
        resp.setMessageKey(response.getMessageKey());
        return resp;
    }
}
