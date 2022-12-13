package com.brandnewdata.mop.poc.bff.service.proxy;

import com.brandnewdata.mop.poc.bff.converter.proxy.ProxyEndpointCallVoConverter;
import com.brandnewdata.mop.poc.bff.vo.proxy.operate.ProxyEndpointCallFilter;
import com.brandnewdata.mop.poc.bff.vo.proxy.operate.ProxyEndpointCallVo;
import com.brandnewdata.mop.poc.bff.vo.proxy.operate.ProxyStatistic;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.proxy.bo.ProxyEndpointFilter;
import com.brandnewdata.mop.poc.proxy.bo.ProxyFilter;
import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointCallDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyAService;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyEndpointAService;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyEndpointCallAService;
import com.brandnewdata.mop.poc.proxy.service.combined.IProxyEndpointCService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProxyOperateBffService {

    private final IProxyEndpointCService proxyEndpointCService;

    private final IProxyEndpointAService proxyEndpointAService;

    private final IProxyAService proxyAtomicService;

    private final IProxyEndpointCallAService proxyEndpointCallService;

    public ProxyOperateBffService(IProxyEndpointCService proxyEndpointCService,
                                  IProxyEndpointAService proxyEndpointAService,
                                  IProxyAService proxyAtomicService,
                                  IProxyEndpointCallAService proxyEndpointCallService) {
        this.proxyEndpointCService = proxyEndpointCService;
        this.proxyEndpointAService = proxyEndpointAService;
        this.proxyAtomicService = proxyAtomicService;
        this.proxyEndpointCallService = proxyEndpointCallService;
    }

    public Page<ProxyEndpointCallVo> page(ProxyEndpointCallFilter filter) {
        Integer pageNum = filter.getPageNum();
        Integer pageSize = filter.getPageSize();

        List<Long> endpointIdList = fetchEndpointIdListByFilter(filter);

        Page<ProxyEndpointCallDto> page = proxyEndpointCallService
                .fetchPageByEndpointId(pageNum, pageSize, endpointIdList);

        List<ProxyEndpointCallVo> voList = page.getRecords().stream()
                .map(ProxyEndpointCallVoConverter::createFrom).collect(Collectors.toList());

        return new Page<>(page.getTotal(), voList);
    }

    public ProxyStatistic statistic(ProxyEndpointCallFilter filter) {
        List<Long> endpointIdList = fetchEndpointIdListByFilter(filter);

        ProxyStatistic statistic = new ProxyStatistic();

        Map<Long, List<ProxyEndpointCallDto>> proxyEndpointCallDtoListMap =
                proxyEndpointCallService.fetchListByEndpointId(endpointIdList);

        return statistic;
    }

    private List<Long> fetchEndpointIdListByFilter(ProxyEndpointCallFilter filter) {
        String proxyName = filter.getProxyName();
        String version = filter.getVersion();
        String location = filter.getLocation();

        // 查询proxy
        ProxyFilter proxyFilter = new ProxyFilter().setName(proxyName).setVersion(version);
        List<ProxyDto> proxyDtoList = proxyAtomicService.fetchListByFilter(proxyFilter);
        List<Long> proxyIdList = proxyDtoList.stream().map(ProxyDto::getId).collect(Collectors.toList());

        // 查询 proxyEndpoint
        ProxyEndpointFilter proxyEndpointFilter = new ProxyEndpointFilter().setLocation(location);
        Map<Long, List<ProxyEndpointDto>> proxyEndpointDtoListMap =
                proxyEndpointAService.fetchListByProxyIdAndFilter(proxyIdList, proxyEndpointFilter);
        return proxyEndpointDtoListMap.values().stream().flatMap(List::stream)
                .map(ProxyEndpointDto::getId).collect(Collectors.toList());
    }
}
