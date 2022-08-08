package com.brandnewdata.mop.poc.proxy.rest;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.proxy.dto.Endpoint;
import com.brandnewdata.mop.poc.proxy.dto.Proxy;
import org.springframework.web.bind.annotation.*;

/**
 * API管理相关的接口
 *
 * @author caiwillie
 */
@RestController
public class ReverseProxyRest {


    /**
     * 新增/更新 API
     *
     * @param reverseProxy the reverse proxy
     * @return the result
     */
    @PostMapping("/rest/reverseProxy/save")
    public Result<Proxy> save(@RequestBody Proxy reverseProxy) {
        return null;
    }

    /**
     * API详情
     *
     * @param id api id
     * @return the result
     */
    @GetMapping("/rest/reverseProxy/detail")
    public Result<Proxy> detail(@RequestParam Long id) {
        return null;
    }


    /**
     * API 分页列表
     *
     * @param pageNum  分页码
     * @param pageSize 分页大小
     * @return the result
     */
    @GetMapping("/rest/reverseProxy/page")
    public Result<Page<Proxy>> page(@RequestParam int pageNum, @RequestParam int pageSize) {
        return null;
    }

    /**
     * 新增/更新 endpoint
     *
     * @param endpoint the endpoint
     * @return the result
     */
    @PostMapping("/rest/reverseProxy/saveEndpoint")
    public Result<Endpoint> saveEndpoint(@RequestBody Endpoint endpoint) {
        return null;
    }

    /**
     * endpoint 详情
     *
     * @param id endpoint 详情
     * @return the result
     */
    @GetMapping("/rest/reverseProxy/detailEndpoint")
    public Result<Endpoint> detailEndpoint(@RequestParam Long id) {
        return null;
    }

    /**
     * endpoint 分页列表
     *
     * @param pageNum  分页码
     * @param pageSize 分页大小
     * @return the result
     */
    @GetMapping("/rest/reverseProxy/pageEndpoint")
    public Result<Endpoint> pageEndpoint(@RequestParam int pageNum, @RequestParam int pageSize) {
        return null;
    }

}
