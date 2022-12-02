package com.brandnewdata.mop.poc.bff.controller.proxy;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.bff.vo.proxy.operate.EndpointCallTimeFilter;
import com.brandnewdata.mop.poc.bff.vo.proxy.operate.EndpointCallTimeVo;
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
     * 分页获取调用列表
     *
     * @param filter the filter
     * @return the page
     */
    @PostMapping(value = "/rest/proxy/oprate/callTime/page")
    public Page<EndpointCallTimeVo> pageCallTime(@RequestBody EndpointCallTimeFilter filter) {
        return null;
    }

    /**
     * 获取调用详情
     *
     * @param callTimeId the call time id
     * @return the call time detail
     */
    @GetMapping(value = "/rest/proxy/oprate/callTime/detail")
    public Result<EndpointCallTimeVo> getCallTimeDetail(@RequestParam Long callTimeId) {
        return null;
    }

}
