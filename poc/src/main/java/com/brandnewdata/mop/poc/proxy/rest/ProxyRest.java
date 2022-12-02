package com.brandnewdata.mop.poc.proxy.rest;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.proxy.dto.old.Endpoint;
import com.brandnewdata.mop.poc.proxy.dto.old.Proxy;
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
public class ProxyRest {

    @Resource
    private ProxyService proxyService;

    @Resource
    private EndpointService endpointService;

    /**
     * 新增/更新 API
     *
     * @param proxy the reverse proxy
     * @return the result
     */
    @PostMapping("/rest/reverseProxy/save")
    public Result<Proxy> save(@RequestBody Proxy proxy) {
        Proxy result = proxyService.save(proxy);
        return Result.OK(result);
    }

    /**
     * API详情
     *
     * @param id api id
     * @return the result
     */
    @GetMapping("/rest/reverseProxy/detail")
    public Result<Proxy> detail(@RequestParam long id) {
        Proxy result = proxyService.getOne(id);
        return Result.OK(result);
    }




    /**
     * endpoint 详情
     *
     * @param id endpoint 详情
     * @return the result
     */
    @GetMapping("/rest/reverseProxy/detailEndpoint")
    public Result<Endpoint> detailEndpoint(@RequestParam long id) {
        Endpoint result = endpointService.getOne(id);
        return Result.OK(result);
    }

    /**
     * endpoint 分页列表
     *
     * @param proxyId  proxy id
     * @param pageNum  分页码
     * @param pageSize 分页大小
     * @return the result
     */
    @GetMapping("/rest/reverseProxy/pageEndpoint")
    public Result<Page<Endpoint>> pageEndpoint(
            @RequestParam Long proxyId,
            @RequestParam int pageNum,
            @RequestParam int pageSize) {
        Page<Endpoint> result = endpointService.page(proxyId, pageNum, pageSize);
        return Result.OK(result);
    }


}
