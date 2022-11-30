package com.brandnewdata.mop.api.process;

import com.brandnewdata.common.webresult.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "poc", contextId = "mopProcess")
public interface IProcessAPI {
    @RequestMapping("/api/process/sendMessage")
    Result<MessageDto> sendMessage(@RequestBody SendMessageDto req);

}
