package com.brandnewdata.mop.poc.bff.controller.proxy;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Opt;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.bff.service.proxy.ProxyOperateBffService;
import com.brandnewdata.mop.poc.bff.vo.proxy.operate.ProxyEndpointCallFilter;
import com.brandnewdata.mop.poc.bff.vo.proxy.operate.ProxyEndpointCallVo;
import com.brandnewdata.mop.poc.bff.vo.proxy.operate.ProxyStatistic;
import com.brandnewdata.mop.poc.common.dto.Page;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * API运行监控相关接口
 *
 * @author caiwillie
 */
@RestController
public class ProxyOperateController {

    private final ProxyOperateBffService proxyOperateBffService;

    public ProxyOperateController(ProxyOperateBffService proxyOperateBffService) {
        this.proxyOperateBffService = proxyOperateBffService;
    }

    /**
     * 获取统计数据
     *
     * @param filter the filter
     * @return the proxy statistic
     */
    @PostMapping("/rest/proxy/operate/call/statistic")
    public Result<ProxyStatistic> statistic (@RequestBody ProxyEndpointCallFilter filter) {

        LocalDateTime minStartTime = Opt.ofNullable(filter.getStartTime()).map(date -> DateUtil.parse(date).toLocalDateTime()).orElse(LocalDateTime.now().minusDays(1));
        LocalDateTime maxStartTime = Opt.ofNullable(filter.getEndTime()).map(date -> DateUtil.parse(date).toLocalDateTime()).orElse(null);

        //添加校验, 时间间隔一个月内
        if(!checkTimeInterval(minStartTime,maxStartTime)){
            throw new RuntimeException("只能查询近30天的数据");
        }

        ProxyStatistic ret = proxyOperateBffService.statistic(filter);
        return Result.OK(ret);
    }

    /**
     * 分页获取调用列表
     *
     * @param filter the filter
     * @return the page
     */
    @PostMapping(value = "/rest/proxy/operate/call/page")
    public Result<Page<ProxyEndpointCallVo>> pageCallTime(@RequestBody ProxyEndpointCallFilter filter) {

        LocalDateTime minStartTime = Opt.ofNullable(filter.getStartTime()).map(date -> DateUtil.parse(date).toLocalDateTime()).orElse(LocalDateTime.now().minusDays(1));
        LocalDateTime maxStartTime = Opt.ofNullable(filter.getEndTime()).map(date -> DateUtil.parse(date).toLocalDateTime()).orElse(null);

        //添加校验, 时间间隔一个月内
        if(!checkTimeInterval(minStartTime,maxStartTime)){
            throw new RuntimeException("只能查询近30天的数据");
        }
        Page<ProxyEndpointCallVo> ret = proxyOperateBffService.page(filter);
        return Result.OK(ret);
    }

    /**
     * 获取调用详情
     *
     * @param callTimeId the call time id
     * @return the call time detail
     */
    @GetMapping(value = "/rest/proxy/operate/call/detail")
    public Result<ProxyEndpointCallVo> getCallTimeDetail(@RequestParam Long callTimeId) {
        return Result.OK();
    }



    private boolean checkTimeInterval(LocalDateTime min, LocalDateTime max){
        Duration duration = Duration.between(min,max);
        Long monthMillis = 2592000000L;
        Long dMillis = duration.toMillis();
        if(Math.abs(dMillis)>monthMillis){
            return false;
        }
        return true;
    }

}
