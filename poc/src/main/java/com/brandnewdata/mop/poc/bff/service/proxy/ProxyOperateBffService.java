package com.brandnewdata.mop.poc.bff.service.proxy;

import com.brandnewdata.mop.poc.bff.vo.proxy.operate.ProxyEndpointCallFilter;
import com.brandnewdata.mop.poc.bff.vo.proxy.operate.ProxyEndpointCallVo;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.proxy.converter.ProxyEndpointDtoConverter;
import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto;
import com.brandnewdata.mop.poc.proxy.service.IProxyEndpointService2;
import com.brandnewdata.mop.poc.proxy.service.IProxyService2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProxyOperateBffService {

    private final IProxyEndpointService2 proxyEndpointService;

    private final IProxyService2 proxyService;

    public ProxyOperateBffService(IProxyEndpointService2 proxyEndpointService,
                                  IProxyService2 proxyService) {
        this.proxyEndpointService = proxyEndpointService;
        this.proxyService = proxyService;
    }

    public Page<ProxyEndpointCallVo> page(ProxyEndpointCallFilter filter) {
        List<ProxyEndpointDto> proxyEndpointDtoList = proxyEndpointService.fetchAll();

        // 查询关联的proxy
        List<Long> proxyIdList = proxyEndpointDtoList.stream().map(ProxyEndpointDto::getProxyId).collect(Collectors.toList());
        Map<Long, ProxyDto> proxyDtoMap = proxyService.fetchById(proxyIdList);

        // 更新proxy信息
        for (ProxyEndpointDto proxyEndpointDto : proxyEndpointDtoList) {
            ProxyDto proxyDto = proxyDtoMap.get(proxyEndpointDto.getProxyId());
            ProxyEndpointDtoConverter.updateFrom(proxyEndpointDto, proxyDto);
        }

    }

}
