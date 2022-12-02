package com.brandnewdata.mop.poc.proxy.rest;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.proxy.req.EndpointReq;
import com.brandnewdata.mop.poc.proxy.req.ImportFromFileReq;
import com.brandnewdata.mop.poc.proxy.req.ProxyReq;
import com.brandnewdata.mop.poc.proxy.resp.ApiResp;
import com.brandnewdata.mop.poc.proxy.resp.InspectResp;
import com.brandnewdata.mop.poc.proxy.service.EndpointService;
import com.brandnewdata.mop.poc.proxy.service.ProxyService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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
     * Endpoint标签列表（不分页）
     *
     * @param proxyId the proxy id
     * @return the result
     */
    @GetMapping("/rest/reverseProxy/listEndpointTags")
    public Result<List<String>> listEndpointTags(@RequestParam Long proxyId) {
        List<String> resp = proxyService.listEndpointTags(proxyId);
        return Result.ok(resp);
    }


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
