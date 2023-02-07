package com.brandnewdata.mop.poc.bff.controller.proxy;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.bff.converter.proxy.ProxyDtoConverter;
import com.brandnewdata.mop.poc.bff.service.proxy.ProxyBffService;
import com.brandnewdata.mop.poc.bff.vo.proxy.*;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;
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
    public Result<ProxyVo> save(@RequestBody ProxyVo proxyVo) {
        ProxyVo ret = proxyBffService.saveProxy(proxyVo);
        return Result.OK(ret);
    }

    /**
     * API 详情
     *
     * @param id the id
     * @return the result
     */
    @GetMapping("/rest/reverseProxy/detail")
    public Result<ProxyVo> detail(@RequestParam long id) {
        ProxyVo proxyVo = proxyBffService.detailProxy(id);
        return Result.OK(proxyVo);
    }

    /**
     * 删除API
     *
     * @param proxyVo the proxy vo
     * @return the result
     */
    @PostMapping("/rest/reverseProxy/delete")
    public Result delete(@RequestBody ProxyVo proxyVo) {
        Long id = proxyVo.getId();
        proxyBffService.deleteProxy(id);
        return Result.OK();
    }

    /**
     * 修改API状态
     *
     * @param proxyVo the proxy vo
     * @return the result
     */
    @PostMapping("/rest/reverseProxy/changeState")
    public Result changeState(@RequestBody ProxyVo proxyVo) {
        ProxyDto dto = ProxyDtoConverter.createFrom(proxyVo);
        ProxyVo ret = proxyBffService.changeProxyState(dto);
        return Result.OK(ret);
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
    public Result<Page<ProxyGroupVo>> pageProxy(@RequestParam String projectId,
                                                @RequestParam int pageNum,
                                                @RequestParam int pageSize,
                                                @RequestParam(required = false) String name,
                                                @RequestParam(required = false) String tags) {
        Page<ProxyGroupVo> ret = proxyBffService.pageProxy(Long.valueOf(projectId), pageNum, pageSize, name, tags);
        return Result.OK(ret);
    }

    /**
     * API 标签列表（不分页）
     *
     * @return the result
     */
    @GetMapping("/rest/reverseProxy/listTags")
    public Result<List<String>> listTags() {
        List<String> ret = proxyBffService.listProxyTag();
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
     * endpoint 详情
     *
     * @param id endpoint 详情
     * @return the result
     */
    @GetMapping("/rest/reverseProxy/detailEndpoint")
    public Result<ProxyEndpointVo> detailEndpoint(@RequestParam String id) {
        ProxyEndpointVo ret = proxyBffService.detailEndpoint(Long.valueOf(id));
        return Result.OK(ret);
    }

    /**
     * 删除Endpoint
     *
     * @param vo the vo
     * @return the result
     */
    @PostMapping("/rest/reverseProxy/deleteEndpoint")
    public Result deleteEndpoint(@RequestBody ProxyEndpointVo vo) {
        String id = vo.getId();
        proxyBffService.deleteEndpoint(Long.valueOf(id));
        return Result.OK();
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
    public Result<Page<ProxyEndpointVo>> pageEndpoint(
            @RequestParam Long proxyId,
            @RequestParam int pageNum,
            @RequestParam int pageSize) {
        Page<ProxyEndpointVo> ret = proxyBffService.pageEndpoint(pageNum, pageSize, proxyId);
        return Result.OK(ret);
    }


    /**
     * Endpoint标签列表（不分页）
     *
     * @param proxyId the proxy id
     * @return the result
     */
    @GetMapping("/rest/reverseProxy/listEndpointTags")
    public Result<List<String>> listEndpointTags(@RequestParam Long proxyId) {
        List<String> ret = proxyBffService.listEndpointTag(proxyId);
        return Result.OK(ret);
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


    /**
     * 查看描述文件
     *
     * @param proxyId the proxy id
     * @param format  the format
     * @return the result
     */
    @GetMapping("/rest/reverseProxy/inspect")
    public Result<InspectVo> inspect(@RequestParam Long proxyId, @RequestParam String format) {
        InspectVo ret = new InspectVo();
        String content = proxyBffService.inspect(proxyId, format);
        ret.setFormat(format);
        ret.setContent(content);
        return Result.OK(ret);
    }

    /**
     * 创建自配置文件
     *
     * @return the result
     */
    @PostMapping("/rest/reverseProxy/importFromFile")
    public Result importFromFile (@RequestBody ImportVo vo) {
        proxyBffService.importProxy(vo.getFileContent(), vo.getFileType());
        return Result.OK();
    }
}
