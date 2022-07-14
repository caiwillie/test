package com.brandnewdata.mop.poc.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.api.ConnectorApi;
import com.brandnewdata.mop.api.dto.RequestParamConfig;
import com.brandnewdata.mop.api.dto.TriggerConfig;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ConnectorApiImpl implements ConnectorApi {

    @Override
    public Result<List<TriggerConfig>> getRequestParamConfig(List<TriggerConfig> configs) {
        try {
            List<RequestParamConfig> requestParams = new ArrayList<>();
            RequestParamConfig requestParam = new RequestParamConfig();
            requestParams.add(requestParam);
            requestParam.setParamName("listenPath");
            requestParam.setParamShowName("监听路径");
            requestParam.setParamType("string");

            Assert.isTrue(CollUtil.isNotEmpty(configs), "触发器不能为空");
            for (int i = 0; i < configs.size(); i++) {
                TriggerConfig triggerConfig = configs.get(i);
                triggerConfig.setRequestParamConfigs(requestParams);
            }
            return Result.OK(configs);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

}
