package com.brandnewdata.mop.poc.api;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.api.ConnectorApi;
import com.brandnewdata.mop.api.dto.TriggerConfig;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ConnectorApiImpl implements ConnectorApi {


    @Override
    public Result<List<TriggerConfig>> getRequestParamConfig(List<TriggerConfig> configs) {
        return null;
    }
}
