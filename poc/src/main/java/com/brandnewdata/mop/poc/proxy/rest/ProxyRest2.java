package com.brandnewdata.mop.poc.proxy.rest;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.proxy.req.ImportFromFileReq;
import com.brandnewdata.mop.poc.proxy.resp.ApiResp;
import com.brandnewdata.mop.poc.proxy.resp.InspectResp;
import com.brandnewdata.mop.poc.proxy.service.EndpointService;
import com.brandnewdata.mop.poc.proxy.service.ProxyService;
import org.springframework.web.bind.annotation.*;

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

    @Resource
    private EndpointService endpointService;




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

    /**
     * 查看描述文件
     *
     * @param proxyId api id
     * @param format  格式：JSON, YAML
     * @return the result
     */
    @GetMapping("/rest/reverseProxy/inspect")
    public Result<InspectResp> inspect(@RequestParam Long proxyId, @RequestParam String format) {
        InspectResp ret = new InspectResp();
        String content = proxyService.inspect(proxyId, format);
        ret.setFormat(format);
        ret.setContent(content);
        return Result.OK(ret);
    }
}
