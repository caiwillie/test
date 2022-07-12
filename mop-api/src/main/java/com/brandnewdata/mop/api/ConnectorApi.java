package com.brandnewdata.mop.api;

import com.brandnewdata.common.webresult.Result;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "poc", contextId = "connectorApi")
public interface ConnectorApi {

    Result getRequestParamConfig();

}
