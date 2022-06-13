package com.brandnewdata.mop.poc.message;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.brandnewdata.mop.poc.common.service.result.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import io.camunda.zeebe.client.ZeebeClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The type Message resource.
 *
 * @author caiwillie
 */

@Slf4j
@RestController
public class MessageResource {

    private static final ObjectMapper OM = new ObjectMapper();
    private static final MapType MAP_TYPE = OM.getTypeFactory().constructMapType(Map.class, String.class, Object.class);

    @Resource
    private ZeebeClient zeebe;

    /**
     * 发送消息
     */
    @PostMapping(value = "/message/send")
    public Result sned(@RequestBody MessageDTO message) {

        String type = message.getType();
        String correlationKey = message.getCorrelationKey();
        String content = message.getContent();

        log.info("接收到消息: type {}, correlationKey {}, content {}",
                type, correlationKey, content);
        Assert.notBlank(type, "消息类型不为空");

        Map<String, Object> values = new HashMap<>();

        // 解析参数
        if(StrUtil.isNotBlank(content)) {
            try {
                Map<String, Object> map = OM.readValue(content, MAP_TYPE);
                if (map != null) values.putAll(map);
            } catch (JsonProcessingException e) {
                log.error("解析json内容异常: {}", content);
                throw new RuntimeException(e);
            }
        }

        zeebe.newPublishMessageCommand()
                .messageName(type)
                .correlationKey(Optional.ofNullable(correlationKey).orElse(StrUtil.EMPTY))
                .variables(values)
                .send()
                .join();

        return Result.success();
    }
}
