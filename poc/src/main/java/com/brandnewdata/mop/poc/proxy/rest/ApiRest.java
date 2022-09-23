package com.brandnewdata.mop.poc.proxy.rest;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.proxy.dto.Proxy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiRest {

    @GetMapping("/rest/reverseProxy/v2/page")
    public Result<Page<Proxy>> page(@RequestParam int pageNum,
                                    @RequestParam int pageSize) {
        return null;
    }

}
