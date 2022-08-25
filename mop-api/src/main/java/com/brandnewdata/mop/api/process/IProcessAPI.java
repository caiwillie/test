package com.brandnewdata.mop.api.process;

import com.brandnewdata.common.webresult.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@FeignClient(name = "poc", contextId = "mopProcess")
public interface IProcessAPI {
    @RequestMapping("/api/process/sendMessage")
    Result<SendMessageResp> sendMessage(@RequestBody SendMessageReq req);

}
