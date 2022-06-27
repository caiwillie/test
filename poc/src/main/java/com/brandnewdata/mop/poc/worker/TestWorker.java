package com.brandnewdata.mop.poc.worker;

import com.brandnewdata.mop.poc.common.service.result.ProcessConstants;
import io.camunda.zeebe.spring.client.annotation.ZeebeVariablesAsType;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
public class TestWorker {
    @ZeebeWorker(type = ProcessConstants.TEST_WORKER, autoComplete = true)
    public Outputs doWork(@ZeebeVariablesAsType Inputs variables) {
        // do whatever you need to do
        // but no need to call client.newCompleteCommand()...

        Outputs outputs = new Outputs();

        Result result = new Result();

        outputs.setResult(result);

        String phone = variables.getPhone();
        if(phone != null && phone.startsWith("182")) {
            result.setAttribution("HZ");
        } else {
            result.setAttribution("JH");
        }

        return outputs;
    }

    @Data
    public static class Inputs {
        // 手机号
        private String phone;
    }

    @Data
    public static class Result {
        // 归属地
        private String attribution;

    }

    @Data
    public static class Outputs {
        // 归属地
        private Result result;

    }
}
