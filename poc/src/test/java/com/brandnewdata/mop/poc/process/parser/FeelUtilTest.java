package com.brandnewdata.mop.poc.process.parser;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.dxy.library.json.jackson.JacksonUtil;
import org.junit.jupiter.api.Test;

import java.util.Map;

class FeelUtilTest {

    @Test
    void evalExpression() {

        String expression = "{\"baseStatus\": baseStatus,  \"data\": data}";
        String str = "{\"msg\":\"\",\"body\":\"{\\\"description\\\":\\\"cdshi001\\\",\\\"alertId\\\":\\\"5SibV2z62FwX\\\",\\\"theme\\\":\\\"simple\\\",\\\"nodeIP\\\":\\\"10.10.10.10\\\",\\\"camearaId\\\":29,\\\"policy\\\":\\\"{\\\\r       \\\\\\\"interval\\\\\\\": 10,\\\\r       \\\\\\\"filters\\\\\\\": [\\\\r           {\\\\r               \\\\\\\"field\\\\\\\": \\\\\\\"person\\\\\\\",\\\\r               \\\\\\\"threshold\\\\\\\": 0.1\\\\r           }\\\\r       ],\\\\r       \\\\\\\"type\\\\\\\": \\\\\\\"simple\\\\\\\"\\\\r   }\\\",\\\"datas\\\":[\\\"29-5SibV2z62FwX-1668052544.5260527-0.jpg\\\",\\\"29-5SibV2z62FwX-1668052544.5278604-1.jpg\\\",\\\"29-5SibV2z62FwX-1668052546.5325038-2.jpg\\\",\\\"29-5SibV2z62FwX-1668052546.534225-3.jpg\\\",\\\"29-5SibV2z62FwX-1668052548.53831-4.jpg\\\"]}\",\"data\":true,\"params\":{\"path\":{},\"query\":{}},\"status\":\"0\",\"headers\":{\"content-length\":\"638\",\"x-forwarded-proto\":\"http\",\"x-forwarded-host\":\"10.100.25.62:9997\",\"postman-token\":\"a9e49631-c6a6-472f-8bd0-a354cfb9cbf7\",\"host\":\"10.244.142.4:9060\",\"g2-domain\":\"http-listener.3gc4y21p2.g2.gongshu.gov.cn\",\"x-forwarded-port\":\"9997\",\"content-type\":\"application/json\",\"accept-encoding\":\"gzip, deflate, br\",\"forwarded\":\"proto=http;host=\\\"10.100.25.62:9997\\\";for=\\\"10.244.166.128:54414\\\"\",\"user-agent\":\"PostmanRuntime/7.28.4\",\"accept\":\"*/*\"},\"baseStatus\":\"OK\"}";
        Map<String, Object> map = JacksonUtil.fromMap(str);
        Object o = FeelUtil.evalExpression(expression, map);
        Map<String, Object> result = FeelUtil.convertMap(o);
        String to = JacksonUtil.to(result);
        return;
    }

    @Test
    void test2() {
        DateTime date1 = DateUtil.parse("2023-01-11 00:00:00");
        return;
    }
}