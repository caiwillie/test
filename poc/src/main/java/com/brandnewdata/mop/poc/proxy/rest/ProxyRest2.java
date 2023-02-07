package com.brandnewdata.mop.poc.proxy.rest;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.proxy.req.ImportFromFileReq;
import com.brandnewdata.mop.poc.proxy.resp.ApiResp;
import com.brandnewdata.mop.poc.proxy.service.ProxyService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * API管理相关的接口
 *
 * @author caiwillie
 */
@RestController
public class ProxyRest2 {

    @Resource
    private ProxyService proxyService;

    /**
     * 创建自配置文件
     *
     * @param req the req
     * @return the result
     */
    @PostMapping("/rest/reverseProxy/importFromFile")
    public Result<ApiResp> importFromFile (@RequestBody ImportFromFileReq req) {
        proxyService.importFromFile(req);
        return Result.OK();
    }
}
