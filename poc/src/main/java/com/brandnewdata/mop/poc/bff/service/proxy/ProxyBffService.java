package com.brandnewdata.mop.poc.bff.service.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Opt;
import com.brandnewdata.mop.poc.bff.converter.proxy.ProxyDtoConverter;
import com.brandnewdata.mop.poc.bff.converter.proxy.ProxyEndpointDtoConverter;
import com.brandnewdata.mop.poc.bff.converter.proxy.ProxyEndpointVoConverter;
import com.brandnewdata.mop.poc.bff.converter.proxy.ProxyVoConverter;
import com.brandnewdata.mop.poc.bff.vo.proxy.*;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyGroupDto;
import com.brandnewdata.mop.poc.proxy.dto.agg.ProxyEndpointCallAgg;
import com.brandnewdata.mop.poc.proxy.dto.filter.ProxyEndpointCallFilter;
import com.brandnewdata.mop.poc.proxy.dto.filter.ProxyEndpointFilter;
import com.brandnewdata.mop.poc.proxy.dto.filter.ProxyFilter;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyAService;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyEndpointAService;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyEndpointCallAService;
import com.brandnewdata.mop.poc.proxy.service.combined.IProxyCService;
import com.brandnewdata.mop.poc.proxy.service.combined.IProxyEndpointCService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProxyBffService {

    private final IProxyEndpointCService proxyEndpointCService;

    private final IProxyEndpointAService proxyEndpointAService;

    private final IProxyEndpointCallAService proxyEndpointCallService;

    private final IProxyCService proxyCService;

    private final IProxyAService proxyAService;

    public ProxyBffService(IProxyEndpointCService proxyEndpointCService,
                           IProxyEndpointAService proxyEndpointAService,
                           IProxyEndpointCallAService proxyEndpointCallService,
                           IProxyCService proxyCService,
                           IProxyAService proxyAService) {
        this.proxyEndpointCService = proxyEndpointCService;
        this.proxyEndpointAService = proxyEndpointAService;
        this.proxyEndpointCallService = proxyEndpointCallService;
        this.proxyCService = proxyCService;
        this.proxyAService = proxyAService;
    }

    public Page<ProxyGroupVo> pageProxy(Long projectId, Integer pageNum, Integer pageSize, String name, String tags) {
        Page<ProxyGroupDto> proxyGroupDtoPage = proxyAService.fetchPageGroupByName(projectId, pageNum, pageSize, name, tags);
        List<ProxyGroupDto> records = proxyGroupDtoPage.getRecords();
        if(CollUtil.isEmpty(records)) return Page.empty();

        List<Long> proxyIdList = new ArrayList<>();

        List<ProxyGroupVo> voList = new ArrayList<>();
        for (ProxyGroupDto proxyGroupDto : records) {
            ProxyGroupVo proxyGroupVo = new ProxyGroupVo();
            proxyGroupVo.setName(proxyGroupDto.getName());
            List<ProxyVo> versions = new ArrayList<>();
            for (ProxyDto proxyDto : proxyGroupDto.getProxyDtoList()) {
                versions.add(ProxyVoConverter.createFrom(proxyDto));
                proxyIdList.add(proxyDto.getId());
            }
            proxyGroupVo.setVersions(versions);
            voList.add(proxyGroupVo);
        }

        // ??????24??????????????????
        // ??????proxyId ?????? endpointList
        ProxyEndpointFilter proxyEndpointFilter = new ProxyEndpointFilter();
        Map<Long, List<ProxyEndpointDto>> proxyEndpointDtoListMap =
                proxyEndpointAService.fetchListByProxyIdAndFilter(proxyIdList, proxyEndpointFilter);
        Map<Long, ProxyEndpointDto> proxyEndpointDtoMap = proxyEndpointDtoListMap.values().stream().flatMap(List::stream)
                .collect(Collectors.toMap(ProxyEndpointDto::getId, Function.identity()));

        // ??????????????????
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime beginTime = endTime.minusDays(1);
        ProxyEndpointCallFilter proxyEndpointCallFilter = new ProxyEndpointCallFilter()
                .setMinStartTime(beginTime).setMaxStartTime(endTime);

        List<ProxyEndpointCallAgg> proxyEndpointCallAggList =
                proxyEndpointCallService.aggProxyEndpointCallByEndpointId(ListUtil.toList(proxyEndpointDtoMap.keySet()), proxyEndpointCallFilter);

        Map<Long, Long> countMap = new HashMap<>();
        for (ProxyEndpointCallAgg agg : proxyEndpointCallAggList) {
            Long endpointId = agg.getEndpointId();
            Long rowCount = agg.getRowCount();
            Long proxyId = Opt.ofNullable(proxyEndpointDtoMap.get(endpointId)).map(ProxyEndpointDto::getProxyId).orElse(null);
            if(proxyId == null) continue;
            countMap.put(proxyId, countMap.getOrDefault(proxyId, 0L) + rowCount);
        }

        // ??????endpoint??????
        Map<Long, Long> endpointCountMap = proxyEndpointAService.countByProxyId(proxyIdList);

        // ????????????
        for (ProxyGroupVo proxyGroupVo : voList) {
            for (ProxyVo version : proxyGroupVo.getVersions()) {
                Long callTimesCount = countMap.getOrDefault(version.getId(), 0L);
                Long endpointCount = endpointCountMap.getOrDefault(version.getId(), 0L);
                version.setCallTimes24h(callTimesCount);
                version.setEndpointTotal(endpointCount);
            }
        }

        return new Page<>(proxyGroupDtoPage.getTotal(), voList);
    }

    public ProxyVo saveProxy(ProxyVo proxyVo) {
        ProxyDto ret = proxyAService.save(ProxyDtoConverter.createFrom(proxyVo), false);
        return ProxyVoConverter.createFrom(ret);
    }

    public ProxyVo detailProxy(Long id) {
        ProxyDto proxyDto = proxyAService.fetchById(ListUtil.of(id)).get(id);
        return ProxyVoConverter.createFrom(proxyDto);
    }
    
    public void deleteProxy(Long id) {
        proxyCService.deleteById(id);
    }

    public ProxyVo changeProxyState(ProxyDto dto) {
        ProxyDto proxyDto = proxyAService.changeState(dto);
        return ProxyVoConverter.createFrom(proxyDto);
    }

    public List<String> listProxyTag() {
        return proxyAService.listTag();
    }

    public ProxyEndpointVo saveEndpoint(ProxyEndpointVo vo) {
        ProxyEndpointDto dto = proxyEndpointCService.save(ProxyEndpointDtoConverter.createFrom(vo));
        return ProxyEndpointVoConverter.createFrom(dto);
    }

    public ProxyEndpointVo detailEndpoint(Long id) {
        ProxyEndpointDto proxyEndpointDto = proxyEndpointAService.fetchByIds(ListUtil.of(id)).get(id);
        return ProxyEndpointVoConverter.createFrom(proxyEndpointDto);
    }

    public void deleteEndpoint(Long id) {
        proxyEndpointAService.deleteById(id);
    }

    public List<String> listEndpointTag(Long proxyId) {
        return proxyEndpointAService.listTag(proxyId);
    }

    public Page<ProxyEndpointVo> pageEndpoint(Integer pageNum, Integer pageSize, Long proxyId) {
        Page<ProxyEndpointDto> page = proxyEndpointAService.pageByProxyId(pageNum, pageSize, proxyId);
        List<ProxyEndpointVo> voList = page.getRecords().stream().map(ProxyEndpointVoConverter::createFrom).collect(Collectors.toList());
        return new Page<>(page.getTotal(), voList);
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

        // ??????proxy??????
        for (ProxyEndpointDto proxyEndpointDto : proxyEndpointDtoList) {
            ProxyDto proxyDto = proxyDtoMap.get(proxyEndpointDto.getProxyId());
            ProxyEndpointDtoConverter.updateFrom(proxyEndpointDto, proxyDto);
        }

        // ????????????????????????
        ListUtil.sort(proxyEndpointDtoList, Comparator.comparing(ProxyEndpointDto::getUpdateTime));

        // ???????????????????????????????????????
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

        // ??????
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

    public String inspect(Long proxyId, String format) {
        return proxyCService.inspect(proxyId, format);
    }

    public void importProxy(String content, String format) {
        proxyCService.importProxy(content, format);
    }

}
