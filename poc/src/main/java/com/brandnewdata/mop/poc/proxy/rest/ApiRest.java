package com.brandnewdata.mop.poc.proxy.rest;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.proxy.req.ImportFromFileReq;
import com.brandnewdata.mop.poc.proxy.resp.ApiResp;
import org.springframework.web.bind.annotation.*;

/**
 * API管理相关的接口
 *
 * @author caiwillie
 */
@RestController
public class ApiRest {

    /**
     * API 分页列表（V2）
     *
     * @param pageNum  分页页码
     * @param pageSize 分页大小
     * @param name     搜索名称
     * @return the result
     */
    @GetMapping("/rest/reverseProxy/v2/page")
    public Result<Page<ApiResp>> page(@RequestParam int pageNum,
                                      @RequestParam int pageSize,
                                      @RequestParam(required = false) String name) {
        return null;
    }

    @GetMapping("/rest/reverseProxy/importFromFile")
    public Result<ApiResp> importFromFile (@RequestBody ImportFromFileReq req) {
        return null;
    }


}
