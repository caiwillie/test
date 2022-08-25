package com.brandnewdata.mop.api.process;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "poc", contextId = "mopProcess")
public interface IProcessAPI {

}
