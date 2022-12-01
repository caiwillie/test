package com.brandnewdata.mop.poc.papi.rest;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.papi.dto.Endpoint;
import com.brandnewdata.mop.poc.papi.dto.Proxy;
import com.brandnewdata.mop.poc.papi.service.EndpointService;
import com.brandnewdata.mop.poc.papi.service.ProxyService;
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
     * 【废弃】API 分页列表
     *
     * @param pageNum  分页码
     * @param pageSize 分页大小
     * @return the result
     */
    @GetMapping("/rest/reverseProxy/page")
    public Result<Page<Proxy>> page(@RequestParam int pageNum, @RequestParam int pageSize) {
        Page<Proxy> result = proxyService.page(pageNum, pageSize);
        return Result.OK(result);
    }

    /**
     * 新增/更新 endpoint
     *
     * @param endpoint the endpoint
     * @return the result
     */
    @PostMapping("/rest/reverseProxy/saveEndpoint")
    public Result<Endpoint> saveEndpoint(@RequestBody Endpoint endpoint) {
        Endpoint result = endpointService.save(endpoint);
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
