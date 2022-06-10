package com.brandnewdata.mop.poc.message;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.camunda.zeebe.client.ZeebeClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Message resource.
 *
 * @author caiwillie
 */

@Slf4j
@RestController
public class MessageResource {

    /**
     * The Zeebe.
     */
    @Resource
    private ZeebeClient zeebe;

    /**
     * 发送消息
     */
    @PostMapping(value = "/message/send")
    public void sned(@RequestBody MessageDTO message) {
        String type = message.getType();
        String correlationKey = message.getCorrelationKey();
        String content = message.getContent();

        log.info("接收到消息: type {}, correlationKey {}, content {}",
                type, correlationKey, content);
        Assert.notBlank(type, "消息类型不为空");

        Map<String, Object> values = new HashMap<>();

        // 解析参数
        if(StrUtil.isNotBlank(content)) {
            Map<String, Object> raw = JSONUtil.parseObj(content).getRaw();
            if (raw != null) values.putAll(raw);
        }

        zeebe.newPublishMessageCommand()
                .messageName(type)
                .correlationKey(correlationKey)
                .variables(values)
                .send()
                .join();
    }
}
