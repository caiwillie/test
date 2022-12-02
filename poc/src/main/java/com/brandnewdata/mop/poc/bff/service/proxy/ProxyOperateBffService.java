package com.brandnewdata.mop.poc.bff.service.proxy;

import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.bff.converter.proxy.ProxyEndpointCallVoConverter;
import com.brandnewdata.mop.poc.bff.converter.proxy.ProxyEndpointDtoConverter;
import com.brandnewdata.mop.poc.bff.vo.proxy.operate.ProxyEndpointCallFilter;
import com.brandnewdata.mop.poc.bff.vo.proxy.operate.ProxyEndpointCallVo;
import com.brandnewdata.mop.poc.common.dto.Page;

import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointCallDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto;
import com.brandnewdata.mop.poc.proxy.service.IProxyEndpointCallService;
import com.brandnewdata.mop.poc.proxy.service.IProxyEndpointService2;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyAtomicService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProxyOperateBffService {

    private final IProxyEndpointService2 proxyEndpointService;

    private final IProxyAtomicService proxyAtomicService;

    private final IProxyEndpointCallService proxyEndpointCallService;

    public ProxyOperateBffService(IProxyEndpointService2 proxyEndpointService,
                                  IProxyAtomicService proxyAtomicService,
                                  IProxyEndpointCallService proxyEndpointCallService) {
        this.proxyEndpointService = proxyEndpointService;
        this.proxyAtomicService = proxyAtomicService;
        this.proxyEndpointCallService = proxyEndpointCallService;
    }

    public Page<ProxyEndpointCallVo> page(ProxyEndpointCallFilter filter) {
        Integer pageNum = filter.getPageNum();
        Integer pageSize = filter.getPageSize();
        String proxyName = filter.getProxyName();
        String version = filter.getVersion();
        String location = filter.getLocation();

        List<ProxyEndpointDto> proxyEndpointDtoList = proxyEndpointService.fetchAll();

        // 查询关联的proxy
        List<Long> proxyIdList = proxyEndpointDtoList.stream().map(ProxyEndpointDto::getProxyId).collect(Collectors.toList());
        Map<Long, ProxyDto> proxyDtoMap = proxyAtomicService.fetchById(proxyIdList);

        // 更新proxy信息
        for (ProxyEndpointDto proxyEndpointDto : proxyEndpointDtoList) {
            ProxyDto proxyDto = proxyDtoMap.get(proxyEndpointDto.getProxyId());
            ProxyEndpointDtoConverter.updateFrom(proxyEndpointDto, proxyDto);
        }

        // 过滤得到符合条件的proxyEndpoint
        List<Long> filterEndpointIdList = proxyEndpointDtoList.stream().filter(proxyEndpointDto -> {
            if (proxyName == null) return true;
            if (!StrUtil.equals(proxyEndpointDto.getProxyName(), proxyName)) return false;
            if (version == null) return true;
            if (!StrUtil.equals(proxyEndpointDto.getProxyVersion(), version)) return false;
            if (location == null) return true;
            return StrUtil.equals(proxyEndpointDto.getLocation(), location);
        }).map(ProxyEndpointDto::getId).collect(Collectors.toList());

        Page<ProxyEndpointCallDto> page = proxyEndpointCallService
                .pageByEndpointId(pageNum, pageSize, filterEndpointIdList);

        List<ProxyEndpointCallVo> voList = page.getRecords().stream()
                .map(ProxyEndpointCallVoConverter::createFrom).collect(Collectors.toList());

        return new Page<>(page.getTotal(), voList);
    }

}
