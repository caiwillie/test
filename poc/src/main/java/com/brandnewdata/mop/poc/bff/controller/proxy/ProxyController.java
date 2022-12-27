package com.brandnewdata.mop.poc.bff.controller.proxy;

import cn.hutool.core.lang.Assert;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.bff.service.proxy.ProxyBffService;
import com.brandnewdata.mop.poc.bff.vo.proxy.ProxyEndpointVo;
import com.brandnewdata.mop.poc.bff.vo.proxy.ProxyGroupVo;
import com.brandnewdata.mop.poc.bff.vo.proxy.ProxyVo;
import com.brandnewdata.mop.poc.bff.vo.proxy.SimpleProxyGroupVo;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.proxy.req.EndpointReq;
import com.brandnewdata.mop.poc.proxy.req.ProxyReq;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API管理相关的接口（新）
 *
 * @author caiwillie
 */
@RestController
public class ProxyController {

    private final ProxyBffService proxyBffService;

    public ProxyController(ProxyBffService proxyBffService) {
        this.proxyBffService = proxyBffService;
    }

    /**
     * 保存 API
     *
     * @param proxyVo the proxy vo
     * @return the result
     */
    @PostMapping("/rest/reverseProxy/save")
    public Result save(@RequestBody ProxyVo proxyVo) {
        proxyBffService.saveProxy(proxyVo);
        return Result.OK();
    }

    /**
     * 删除API
     *
     * @param proxyReq the proxy req
     * @return the result
     */
    @PostMapping("/rest/reverseProxy/delete")
    public Result delete(@RequestBody ProxyReq proxyReq) {
        Long id = proxyReq.getId();
        // proxyService.delete(id);
        return Result.OK();
    }

    /**
     * API 分页列表
     *
     * @param pageNum  分页页码
     * @param pageSize 分页大小
     * @param name     搜索名称
     * @param tags     标签（多个用‘,’分隔）
     * @return the result
     */
    @GetMapping("/rest/proxy/page")
    public Result<Page<ProxyGroupVo>> pageProxy(@RequestParam int pageNum,
                                                @RequestParam int pageSize,
                                                @RequestParam(required = false) String name,
                                                @RequestParam(required = false) String tags) {
        Page<ProxyGroupVo> ret = proxyBffService.pageProxy(pageNum, pageSize, name, tags);
        return Result.OK(ret);
    }


    /**
     * 保存 endpoint
     *
     * @param vo the endpoint
     * @return the result
     */
    @PostMapping("/rest/proxy/endpoint/save")
    public Result<ProxyEndpointVo> saveEndpoint(@RequestBody ProxyEndpointVo vo) {
        ProxyEndpointVo ret = proxyBffService.saveEndpoint(vo);
        return Result.OK(ret);
    }

    /**
     * 删除Endpoint
     *
     * @param req the req
     * @return the result
     */
    @PostMapping("/rest/reverseProxy/deleteEndpoint")
    public Result deleteEndpoint(@RequestBody EndpointReq req) {
        Long id = req.getId();
        Assert.notNull(id, "路径 id 不能为空");
        // endpointService.deleteByIdList(ListUtil.of(id));
        return Result.OK();
    }

    /**
     * 获取所有 api-版本-路径
     *
     * @return the all proxy
     */
    @GetMapping(value = "/rest/proxy/oprate/getAllProxy")
    public Result<List<SimpleProxyGroupVo>> getAllProxy() {
        List<SimpleProxyGroupVo> ret = proxyBffService.getAllProxy();
        return Result.OK(ret);
    }

}
