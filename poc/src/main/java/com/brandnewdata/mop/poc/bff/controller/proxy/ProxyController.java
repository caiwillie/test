package com.brandnewdata.mop.poc.bff.controller.proxy;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.bff.service.proxy.ProxyBffService;
import com.brandnewdata.mop.poc.bff.vo.proxy.ProxyEndpointVo;
import com.brandnewdata.mop.poc.bff.vo.proxy.SimpleProxyVo;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.proxy.resp.ApiResp;
import com.brandnewdata.mop.poc.proxy.service.ProxyService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * API管理相关的接口（新）
 *
 * @author caiwillie
 */
@RestController
public class ProxyController {

    @Resource
    private ProxyService proxyService;

    private final ProxyBffService proxyBffService;

    public ProxyController(ProxyBffService proxyBffService) {
        this.proxyBffService = proxyBffService;
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
    public Result<Page<ApiResp>> pageProxy(@RequestParam int pageNum,
                                           @RequestParam int pageSize,
                                           @RequestParam(required = false) String name,
                                           @RequestParam(required = false) String tags) {
        Page<ApiResp> resp = proxyService.pageV2(pageNum, pageSize, name, tags);
        return Result.OK(resp);
    }


    /**
     * 保存 endpoint
     *
     * @param vo the endpoint
     * @return the result
     */
    @PostMapping("/rest/proxy/endpoint/save")
    public Result<ProxyEndpointVo> saveEndpoint(@RequestBody ProxyEndpointVo vo) {
        ProxyEndpointVo ret = proxyBffService.save(vo);
        return Result.OK(ret);
    }

    /**
     * 获取所有 api-版本-路径
     *
     * @return the all proxy
     */
    @GetMapping(value = "/rest/proxy/oprate/getAllProxy")
    public Result<List<SimpleProxyVo>> getAllProxy() {
        return null;
    }

}
