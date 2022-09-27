package com.brandnewdata.mop.poc.proxy.rest;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.proxy.req.ImportFromFileReq;
import com.brandnewdata.mop.poc.proxy.req.ProxyReq;
import com.brandnewdata.mop.poc.proxy.resp.ApiResp;
import com.brandnewdata.mop.poc.proxy.resp.InspectResp;
import com.brandnewdata.mop.poc.proxy.service.EndpointService;
import com.brandnewdata.mop.poc.proxy.service.ProxyService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.swing.plaf.ListUI;
import java.util.List;

/**
 * API管理相关的接口
 *
 * @author caiwillie
 */
@RestController
public class ApiRest {

    @Resource
    private ProxyService proxyService;

    @Resource
    private EndpointService endpointService;

    /**
     * 标签列表（不分页）
     *
     * @return the result
     */
    @GetMapping("/rest/reverseProxy/listTags")
    public Result<List<String>> listTags() {
        List<String> tags = proxyService.listTags();
        return Result.OK(tags);
    }

    /**
     * 【V2】API 分页列表
     *
     * @param pageNum  分页页码
     * @param pageSize 分页大小
     * @param name     搜索名称
     * @param tags     标签（多个用‘,’分隔）
     * @return the result
     */
    @GetMapping("/rest/reverseProxy/v2/page")
    public Result<Page<ApiResp>> page(@RequestParam int pageNum,
                                      @RequestParam int pageSize,
                                      @RequestParam(required = false) String name,
                                      @RequestParam(required = false) String tags) {
        Page<ApiResp> resp = proxyService.pageV2(pageNum, pageSize, name, tags);
        return Result.OK(resp);
    }

    /**
     * 创建自配置文件
     *
     * @param req the req
     * @return the result
     */
    @GetMapping("/rest/reverseProxy/importFromFile")
    public Result<ApiResp> importFromFile (@RequestBody ImportFromFileReq req) {
        return null;
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
        proxyService.delete(id);
        return Result.OK();
    }

    /**
     * 更改API的状态
     *
     * @param req the proxy req
     * @return the result
     */
    @PostMapping("/rest/reverseProxy/changeState")
    public Result changeState(@RequestBody ProxyReq req) {
        Long id = req.getId();
        Integer state = req.getState();
        proxyService.changeState(id, state);
        return Result.OK();
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
        endpointService.deleteByIdList(ListUtil.of(id));
        return Result.OK();
    }

    /**
     * 查看描述文件
     *
     * @param proxyReq the proxy req
     * @return the result
     */
    @PostMapping("/rest/reverseProxy/inspect")
    public Result<InspectResp> inspect(@RequestBody ProxyReq proxyReq) {
        return null;
    }


}
