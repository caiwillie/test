package com.brandnewdata.mop.poc.bff.controller.proxy;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.bff.vo.proxy.operate.ProxyEndpointCallFilter;
import com.brandnewdata.mop.poc.bff.vo.proxy.operate.ProxyEndpointCallVo;
import com.brandnewdata.mop.poc.bff.vo.proxy.operate.ProxyStatistic;
import com.brandnewdata.mop.poc.common.dto.Page;
import org.springframework.web.bind.annotation.*;

/**
 * API运行监控相关接口
 *
 * @author caiwillie
 */
@RestController
public class ProxyOperateController {

    /**
     * 获取统计数据
     *
     * @param filter the filter
     * @return the proxy statistic
     */
    @PostMapping("/proxy/operate/call/statistic")
    public ProxyStatistic statistic (@RequestBody ProxyEndpointCallFilter filter) {
        return null;
    }

    /**
     * 分页获取调用列表
     *
     * @param filter the filter
     * @return the page
     */
    @PostMapping(value = "/rest/proxy/operate/call/page")
    public Page<ProxyEndpointCallVo> pageCallTime(@RequestBody ProxyEndpointCallFilter filter) {
        return null;

    }

    /**
     * 获取调用详情
     *
     * @param callTimeId the call time id
     * @return the call time detail
     */
    @GetMapping(value = "/rest/proxy/operate/call/detail")
    public Result<ProxyEndpointCallVo> getCallTimeDetail(@RequestParam Long callTimeId) {
        return null;
    }


}
