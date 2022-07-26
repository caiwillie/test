package com.brandnewdata.mop.poc.operater.rest;

import com.brandnewdata.common.webresult.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OperateRest {

    @GetMapping("/rest/operate/processDefinition/page")
    public Result processDefinitionPage(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize) {

        return null;
    }

}
