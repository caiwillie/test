package com.brandnewdata.mop.poc.bff.service.proxy;

import cn.hutool.core.collection.ListUtil;
import com.brandnewdata.mop.poc.bff.converter.proxy.ProxyEndpointDtoConverter;
import com.brandnewdata.mop.poc.bff.converter.proxy.ProxyEndpointVoConverter;
import com.brandnewdata.mop.poc.bff.vo.proxy.ProxyEndpointVo;
import com.brandnewdata.mop.poc.bff.vo.proxy.SimpleProxyVersionEndpointVo;
import com.brandnewdata.mop.poc.bff.vo.proxy.SimpleProxyVersionVo;
import com.brandnewdata.mop.poc.bff.vo.proxy.SimpleProxyVo;
import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto;
import com.brandnewdata.mop.poc.proxy.service.combined.IProxyEndpointCService;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyAService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProxyBffService {

    private final IProxyEndpointCService proxyEndpointService2;

    private final IProxyEndpointCService proxyEndpointService;

    private final IProxyAService proxyAtomicService;

    public ProxyBffService(IProxyEndpointCService proxyEndpointService2,
                           IProxyEndpointCService proxyEndpointService,
                           IProxyAService proxyAtomicService) {
        this.proxyEndpointService2 = proxyEndpointService2;
        this.proxyEndpointService = proxyEndpointService;
        this.proxyAtomicService = proxyAtomicService;
    }

    public ProxyEndpointVo save(ProxyEndpointVo vo) {
        ProxyEndpointDto dto = proxyEndpointService2.save(ProxyEndpointDtoConverter.createFrom(vo));
        return ProxyEndpointVoConverter.createFrom(dto);
    }

    public List<SimpleProxyVo> getAllProxy() {
        List<ProxyEndpointDto> proxyEndpointDtoList = proxyEndpointService.fetchAll();

        // 查询关联的proxy
        List<Long> proxyIdList = proxyEndpointDtoList.stream().map(ProxyEndpointDto::getProxyId).collect(Collectors.toList());
        Map<Long, ProxyDto> proxyDtoMap = proxyAtomicService.fetchById(proxyIdList);

        // 更新proxy信息
        for (ProxyEndpointDto proxyEndpointDto : proxyEndpointDtoList) {
            ProxyDto proxyDto = proxyDtoMap.get(proxyEndpointDto.getProxyId());
            ProxyEndpointDtoConverter.updateFrom(proxyEndpointDto, proxyDto);
        }

        // 根据更新时间排序
        ListUtil.sort(proxyEndpointDtoList, Comparator.comparing(ProxyEndpointDto::getUpdateTime));

        // 按照顺序依次放入不同集合中
        LinkedHashMap<String, SimpleProxyVo> proxyMap = new LinkedHashMap<>();
        Map<String, LinkedHashMap<String, SimpleProxyVersionVo>> proxyVersionMapMap = new HashMap<>();
        Map<String, LinkedHashMap<String, SimpleProxyVersionEndpointVo>> proxyVersionEndpointMapMap = new HashMap<>();

        for (ProxyEndpointDto proxyEndpointDto : proxyEndpointDtoList) {
            String proxyName = proxyEndpointDto.getProxyName();
            String proxyVersion = proxyEndpointDto.getProxyVersion();
            String location = proxyEndpointDto.getLocation();


            if(!proxyMap.containsKey(proxyName)) {
                SimpleProxyVo proxyVo = new SimpleProxyVo();
                proxyVo.setName(proxyName);
                proxyMap.put(proxyName, proxyVo);
            }

            LinkedHashMap<String, SimpleProxyVersionVo> proxyVersionMap =
                    proxyVersionMapMap.computeIfAbsent(proxyName, k -> new LinkedHashMap<>());
            if(!proxyVersionMap.containsKey(proxyVersion)) {
                SimpleProxyVersionVo proxyVersionVo = new SimpleProxyVersionVo();
                proxyVersionVo.setVersion(proxyVersion);
                proxyVersionMap.put(proxyVersion, proxyVersionVo);
            }

            LinkedHashMap<String, SimpleProxyVersionEndpointVo> proxyVersionEndpointMap =
                    proxyVersionEndpointMapMap.computeIfAbsent(proxyVersion, k -> new LinkedHashMap<>());
            if(!proxyVersionEndpointMap.containsKey(location)) {
                SimpleProxyVersionEndpointVo proxyVersionEndpointVo = new SimpleProxyVersionEndpointVo();
                proxyVersionEndpointVo.setEndpointId(proxyEndpointDto.getId());
                proxyVersionEndpointVo.setLocation(location);
                proxyVersionEndpointMap.put(location, proxyVersionEndpointVo);
            }
        }

        // 组合
        for (SimpleProxyVo proxyVo : proxyMap.values()) {
            String name = proxyVo.getName();
            List<SimpleProxyVersionVo> proxyVersionVoList = ListUtil.toList(proxyVersionMapMap.get(name).values());
            proxyVo.setVersionList(proxyVersionVoList);
            for (SimpleProxyVersionVo proxyVersionVo : proxyVersionVoList) {
                List<SimpleProxyVersionEndpointVo> proxyVersionEndpointVoList =
                        ListUtil.toList(proxyVersionEndpointMapMap.get(proxyVersionVo.getVersion()).values());
                proxyVersionVo.setEndpointList(proxyVersionEndpointVoList);
            }
        }

        return ListUtil.toList(proxyMap.values());
    }

}
