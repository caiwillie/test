package com.brandnewdata.mop.poc.operate.rest;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.process.dto.ProcessInstance;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InstanceRest {

    /**
     * 流程实例分页列表
     *
     * @param pageNum  分页页码
     * @param pageSize 分页大小
     * @return result
     */
    @GetMapping("/rest/operate/instance/page")
    public Result<Page<ProcessInstance>> page (
            @RequestParam int pageNum,
            @RequestParam int pageSize) {
        return null;
    }

}
