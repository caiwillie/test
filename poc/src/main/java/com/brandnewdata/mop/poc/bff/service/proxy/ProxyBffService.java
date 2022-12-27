package com.brandnewdata.mop.poc.bff.service.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.brandnewdata.mop.poc.bff.converter.proxy.ProxyDtoConverter;
import com.brandnewdata.mop.poc.bff.converter.proxy.ProxyEndpointDtoConverter;
import com.brandnewdata.mop.poc.bff.converter.proxy.ProxyEndpointVoConverter;
import com.brandnewdata.mop.poc.bff.converter.proxy.ProxyVoConverter;
import com.brandnewdata.mop.poc.bff.vo.proxy.*;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.proxy.bo.ProxyEndpointFilter;
import com.brandnewdata.mop.poc.proxy.bo.ProxyFilter;
import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyGroupDto;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyAService;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyEndpointAService;
import com.brandnewdata.mop.poc.proxy.service.combined.IProxyCService;
import com.brandnewdata.mop.poc.proxy.service.combined.IProxyEndpointCService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProxyBffService {

    private final IProxyEndpointCService proxyEndpointCService;

    private final IProxyEndpointAService proxyEndpointAService;

    private final IProxyCService proxyCService;

    private final IProxyAService proxyAService;

    public ProxyBffService(IProxyEndpointCService proxyEndpointCService,
                           IProxyEndpointAService proxyEndpointAService,
                           IProxyCService proxyCService,
                           IProxyAService proxyAService) {
        this.proxyEndpointCService = proxyEndpointCService;
        this.proxyEndpointAService = proxyEndpointAService;
        this.proxyCService = proxyCService;
        this.proxyAService = proxyAService;
    }

    public Page<ProxyGroupVo> pageProxy(Integer pageNum, Integer pageSize, String name, String tags) {
        Page<ProxyGroupDto> proxyGroupDtoPage = proxyAService.fetchPageGroupByName(pageNum, pageSize, name, tags);
        List<ProxyGroupDto> records = proxyGroupDtoPage.getRecords();
        if(CollUtil.isEmpty(records)) return Page.empty();

        List<ProxyGroupVo> voList = new ArrayList<>();
        for (ProxyGroupDto record : records) {
            ProxyGroupVo proxyGroupVo = new ProxyGroupVo();
            proxyGroupVo.setName(record.getName());
            proxyGroupVo.setVersions(record.getProxyDtoList()
                    .stream().map(ProxyVoConverter::createFrom).collect(Collectors.toList()));
            voList.add(proxyGroupVo);
        }

        return new Page<>(proxyGroupDtoPage.getTotal(), voList);
    }

    public void saveProxy(ProxyVo proxyVo) {
        proxyAService.save(ProxyDtoConverter.createFrom(proxyVo), false);
    }

    public void deleteProxy(Long id) {
        proxyCService.deleteById(id);
    }

    public ProxyEndpointVo saveEndpoint(ProxyEndpointVo vo) {
        ProxyEndpointDto dto = proxyEndpointCService.save(ProxyEndpointDtoConverter.createFrom(vo));
        return ProxyEndpointVoConverter.createFrom(dto);
    }

    public List<SimpleProxyGroupVo> getAllProxy() {
        // fetch proxy
        List<ProxyDto> proxyDtoList = proxyAService.fetchCacheListByFilter(new ProxyFilter());
        Map<Long, ProxyDto> proxyDtoMap = proxyDtoList.stream().collect(Collectors.toMap(ProxyDto::getId, Function.identity()));

        // fetch proxy endpoint
        Map<Long, List<ProxyEndpointDto>> proxyEndpointDtoListMap = proxyEndpointAService.fetchListByProxyIdAndFilter(
                ListUtil.toList(proxyDtoMap.keySet()), new ProxyEndpointFilter());
        List<ProxyEndpointDto> proxyEndpointDtoList = proxyEndpointDtoListMap.values().stream()
                .flatMap(Collection::stream).collect(Collectors.toList());

        // 更新proxy信息
        for (ProxyEndpointDto proxyEndpointDto : proxyEndpointDtoList) {
            ProxyDto proxyDto = proxyDtoMap.get(proxyEndpointDto.getProxyId());
            ProxyEndpointDtoConverter.updateFrom(proxyEndpointDto, proxyDto);
        }

        // 根据更新时间排序
        ListUtil.sort(proxyEndpointDtoList, Comparator.comparing(ProxyEndpointDto::getUpdateTime));

        // 按照顺序依次放入不同集合中
        LinkedHashMap<String, SimpleProxyGroupVo> proxyMap = new LinkedHashMap<>();
        Map<String, LinkedHashMap<String, SimpleProxyVo>> proxyVersionMapMap = new HashMap<>();
        Map<String, LinkedHashMap<String, SimpleProxyEndpointVo>> proxyVersionEndpointMapMap = new HashMap<>();

        for (ProxyEndpointDto proxyEndpointDto : proxyEndpointDtoList) {
            String proxyName = proxyEndpointDto.getProxyName();
            String proxyVersion = proxyEndpointDto.getProxyVersion();
            String location = proxyEndpointDto.getLocation();


            if(!proxyMap.containsKey(proxyName)) {
                SimpleProxyGroupVo proxyVo = new SimpleProxyGroupVo();
                proxyVo.setName(proxyName);
                proxyMap.put(proxyName, proxyVo);
            }

            LinkedHashMap<String, SimpleProxyVo> proxyVersionMap =
                    proxyVersionMapMap.computeIfAbsent(proxyName, k -> new LinkedHashMap<>());
            if(!proxyVersionMap.containsKey(proxyVersion)) {
                SimpleProxyVo proxyVersionVo = new SimpleProxyVo();
                proxyVersionVo.setVersion(proxyVersion);
                proxyVersionMap.put(proxyVersion, proxyVersionVo);
            }

            LinkedHashMap<String, SimpleProxyEndpointVo> proxyVersionEndpointMap =
                    proxyVersionEndpointMapMap.computeIfAbsent(proxyVersion, k -> new LinkedHashMap<>());
            if(!proxyVersionEndpointMap.containsKey(location)) {
                SimpleProxyEndpointVo proxyVersionEndpointVo = new SimpleProxyEndpointVo();
                proxyVersionEndpointVo.setEndpointId(proxyEndpointDto.getId());
                proxyVersionEndpointVo.setLocation(location);
                proxyVersionEndpointMap.put(location, proxyVersionEndpointVo);
            }
        }

        // 组合
        for (SimpleProxyGroupVo proxyVo : proxyMap.values()) {
            String name = proxyVo.getName();
            List<SimpleProxyVo> proxyVersionVoList = ListUtil.toList(proxyVersionMapMap.get(name).values());
            proxyVo.setVersionList(proxyVersionVoList);
            for (SimpleProxyVo proxyVersionVo : proxyVersionVoList) {
                List<SimpleProxyEndpointVo> proxyVersionEndpointVoList =
                        ListUtil.toList(proxyVersionEndpointMapMap.get(proxyVersionVo.getVersion()).values());
                proxyVersionVo.setEndpointList(proxyVersionEndpointVoList);
            }
        }

        return ListUtil.toList(proxyMap.values());
    }

}
