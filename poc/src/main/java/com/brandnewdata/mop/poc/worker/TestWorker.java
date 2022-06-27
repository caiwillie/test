package com.brandnewdata.mop.poc.worker;

import cn.hutool.json.JSONUtil;
import com.brandnewdata.mop.poc.common.service.result.ProcessConstants;
import io.camunda.zeebe.spring.client.annotation.ZeebeVariablesAsType;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TestWorker {
    @ZeebeWorker(type = ProcessConstants.TEST_WORKER, autoComplete = true)
    public Outputs doWork(@ZeebeVariablesAsType Inputs variables) {
        // do whatever you need to do
        // but no need to call client.newCompleteCommand()...

        Outputs outputs = new Outputs();

        String phone = variables.getPhone();
        if(phone != null && phone.startsWith("182")) {
            outputs.setAttribution("HZ");
        } else {
            outputs.setAttribution("JH");
        }

        return outputs;
    }

    @Data
    public static class Inputs {
        // 手机号
        private String phone;
    }

    @Data
    public static class Outputs {
        // 归属地
        private String attribution;

        private String jsonSerialization = "{\"english\":\"hello\",\"chinese\":\"你好\"}";

    }
}
