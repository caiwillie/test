package com.brandnewdata.mop.poc.bff.service.homepage;

import cn.hutool.core.collection.CollUtil;
import com.brandnewdata.mop.poc.bff.bo.HomeApiStatisticCountBo;
import com.brandnewdata.mop.poc.proxy.dto.filter.ProxyEndpointFilter;
import com.brandnewdata.mop.poc.proxy.dto.filter.ProxyFilter;
import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyAService;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyEndpointAService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HomeApiStatisticService {

    private final IProxyAService proxyAService;

    private final IProxyEndpointAService proxyEndpointAService;

    public HomeApiStatisticService(IProxyAService proxyAService,
                                   IProxyEndpointAService proxyEndpointAService) {
        this.proxyAService = proxyAService;
        this.proxyEndpointAService = proxyEndpointAService;
    }

    public HomeApiStatisticCountBo statisticCount() {
        HomeApiStatisticCountBo ret = new HomeApiStatisticCountBo(0, 0);
        List<ProxyDto> proxyDtoList = proxyAService.fetchCacheListByFilter(new ProxyFilter());
        if(CollUtil.isEmpty(proxyDtoList)) return ret;
        List<Long> proxyIdList = proxyDtoList.stream().map(ProxyDto::getId).collect(Collectors.toList());
        int proxyCount = proxyIdList.size();
        Map<Long, List<ProxyEndpointDto>> proxyEndpointMap =
                proxyEndpointAService.fetchListByProxyIdAndFilter(proxyIdList, new ProxyEndpointFilter());
        long endpointCount = proxyEndpointMap.values().stream().mapToLong(List::size).sum();
        ret.setApiCount(proxyCount);
        ret.setApiPathCount((int) endpointCount);
        return ret;
    }

}
