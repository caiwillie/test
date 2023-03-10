package com.brandnewdata.mop.api.process;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.api.process.dto.MessageDto;
import com.brandnewdata.mop.api.process.dto.SendMessageDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "poc", contextId = "processApi")
public interface IProcessApi {
    @RequestMapping("/api/process/sendMessage")
    Result<MessageDto> sendMessage(@RequestBody SendMessageDto req);

}
