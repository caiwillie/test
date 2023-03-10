package com.brandnewdata.mop.poc.proxy.service.atomic;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.PageUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.constant.ProxyConst;
import com.brandnewdata.mop.poc.proxy.cache.ProxyCache;
import com.brandnewdata.mop.poc.proxy.converter.ProxyDtoConverter;
import com.brandnewdata.mop.poc.proxy.converter.ProxyPoConverter;
import com.brandnewdata.mop.poc.proxy.dao.ProxyDao;
import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyGroupDto;
import com.brandnewdata.mop.poc.proxy.dto.filter.ProxyFilter;
import com.brandnewdata.mop.poc.proxy.po.ProxyPo;
import com.brandnewdata.mop.poc.util.CollectorsUtil;
import com.brandnewdata.mop.poc.util.PageEnhancedUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProxyAService implements IProxyAService {
    @Resource
    private ProxyDao proxyDao;

    private final ProxyCache proxyCache;

    private final String domainRegEx;

    private final String domainPattern;

    public ProxyAService(ProxyCache proxyCache,
                         @Value("${brandnewdata.api.domainRegEx}") String domainRegEx,
                         @Value("${brandnewdata.api.domainPattern}") String domainPattern) {
        this.proxyCache = proxyCache;
        this.domainRegEx = domainRegEx;
        this.domainPattern = domainPattern;
    }

    @Override
    public Page<ProxyGroupDto> fetchPageGroupByName(Long projectId, Integer pageNum, Integer pageSize,
                                                    String name, String tags) {
        Assert.notNull(projectId, "projectId can not be null");
        Assert.notNull(pageNum > 0, "pageNum must be greater than 0");
        Assert.notNull(pageSize > 0, "pageSize must be greater than 0");

        ProxyFilter proxyFilter = new ProxyFilter().setProjectId(projectId).setName(name).setTags(tags);
        List<ProxyDto> proxyDtoList = fetchListByFilter(proxyFilter);

        Map<String, List<ProxyDto>> proxyDtoListMap = new LinkedHashMap<>();
        proxyDtoList.stream().sorted(Comparator.comparing(ProxyDto::getUpdateTime))
                .collect(Collectors.groupingBy(ProxyDto::getName,
                        CollectorsUtil.toSortedList((o1, o2) -> o2.getUpdateTime().compareTo(o1.getUpdateTime()))))
                .entrySet().stream()
                .sorted((o1, o2) -> o2.getValue().get(0).getUpdateTime().compareTo(o1.getValue().get(0).getUpdateTime()))
                .forEach(entry -> proxyDtoListMap.put(entry.getKey(), entry.getValue()));

        PageUtil.setFirstPageNo(1);
        List<String> nameList = PageEnhancedUtil.slice(pageNum, pageSize, ListUtil.toList(proxyDtoListMap.keySet()));

        // ????????????
        List<ProxyGroupDto> ret = new ArrayList<>();
        for (String _name : nameList) {
            ProxyGroupDto proxyGroupDto = new ProxyGroupDto();
            proxyGroupDto.setName(_name);
            proxyGroupDto.setProxyDtoList(proxyDtoListMap.get(_name));
            ret.add(proxyGroupDto);
        }

        return new Page<>(proxyDtoListMap.size(), ret);
    }

    @Override
    public List<ProxyDto> fetchCacheListByFilter(ProxyFilter filter) {
        String name = filter.getName();
        String version = filter.getVersion();
        String tags = filter.getTags();
        Long projectId = filter.getProjectId();
        LocalDateTime minStartTime = filter.getMinStartTime();
        LocalDateTime maxStartTime = filter.getMaxStartTime();

        // filter
        return proxyCache.asMap().values().stream().filter(proxyDto -> {
            if (StrUtil.isNotBlank(name) && !StrUtil.equals(proxyDto.getName(), name)) return false;
            if (StrUtil.isNotBlank(version) && !StrUtil.equals(proxyDto.getVersion(), version)) return false;
            if(StrUtil.isNotBlank(tags) && !StrUtil.contains(tags, proxyDto.getTag())) return false;
            if(projectId != null && !projectId.equals(proxyDto.getProjectId())) return false;
            // ???????????????????????????????????????
            LocalDateTime createTime = proxyDto.getCreateTime();
            if(minStartTime != null && minStartTime.compareTo(createTime) > 0) return false;
            if(maxStartTime != null && maxStartTime.compareTo(createTime) < 0) return false;
            return true;
        }).collect(Collectors.toList());
    }

    @Override
    public ProxyDto save(ProxyDto proxyDto, boolean imported) {
        String name = proxyDto.getName();
        String version = proxyDto.getVersion();
        Assert.notNull(proxyDto.getProjectId(), "project id can not be null");
        Assert.notNull(name, "API??????????????????");
        Assert.notNull(version, "API??????????????????");

        Long id = proxyDto.getId();
        if(id == null) {
            // ?????????????????????
            Assert.isFalse(existByNameAndVersion(name, version), "{} {} ?????????", name, version);

            // ???????????????????????????
            String domainIdentifier = null;
            do {
                domainIdentifier = RandomUtil.randomString(9);
            } while (existByDomainIdentifier(domainIdentifier));
            proxyDto.setDomain(domainIdentifier);

            if(!imported) {
                proxyDto.setState(ProxyConst.PROXY_STATE__STOPPED);
            } else {
                proxyDto.setState(ProxyConst.PROXY_STATE__DEVELOPING);
            }

            ProxyPo po = ProxyPoConverter.createFrom(proxyDto);
            proxyDao.insert(po);
            proxyDto.setId(po.getId());

            return proxyDto;
        } else {
            ProxyDto oldProxyDto = fetchById(ListUtil.of(id)).get(id);
            Assert.notNull(oldProxyDto, "API?????????");
            Assert.isTrue(StrUtil.equals(oldProxyDto.getName(), name), "API??????????????????");
            Assert.isTrue(StrUtil.equals(oldProxyDto.getVersion(), version), "API??????????????????");

            ProxyPoConverter.updateFrom(oldProxyDto, proxyDto);
            // ???????????????domain?????????null
            oldProxyDto.setDomain(null);
            proxyDao.updateById(ProxyPoConverter.createFrom(oldProxyDto));

            return oldProxyDto;
        }
    }

    @Override
    public Map<Long, ProxyDto> fetchById(List<Long> idList) {
        if(CollUtil.isEmpty(idList)) return MapUtil.empty();
        Assert.isFalse(CollUtil.hasNull(idList), "idList can not contain null");

        QueryWrapper<ProxyPo> query = new QueryWrapper<>();
        query.in(ProxyPo.ID, idList);

        List<ProxyPo> proxyPoList = proxyDao.selectList(query);
        return proxyPoList.stream().map(po -> ProxyDtoConverter.createFrom(po, domainPattern))
                .collect(Collectors.toMap(ProxyDto::getId, Function.identity()));
    }

    @Override
    public ProxyDto fetchByDomain(String domain) {
        // ??????domain?????????????????????????????????????????????????????????
        String identity = ReUtil.getGroup1(domainRegEx, domain);
        QueryWrapper<ProxyPo> query = new QueryWrapper<>();
        query.isNull(ProxyPo.DELETE_FLAG);
        query.eq(ProxyPo.DOMAIN, identity);
        ProxyPo proxyPo = proxyDao.selectOne(query);
        return ProxyDtoConverter.createFrom(proxyPo, domainRegEx);
    }

    @Override
    public ProxyDto changeState(ProxyDto proxyDto) {
        Long id = proxyDto.getId();
        Assert.notNull(id, "api id must not null");
        ProxyPo proxyPo = proxyDao.selectById(id);
        Assert.notNull(proxyPo, "api id is not exist");
        Integer state = proxyPo.getState();
        Assert.isTrue(state != null
                        && (state == ProxyConst.PROXY_STATE__STOPPED || state == ProxyConst.PROXY_STATE__RUNNING),
                "????????????????????????????????? ???????????????");
        proxyPo.setState(proxyDto.getState());
        proxyDao.updateById(proxyPo);
        return ProxyDtoConverter.createFrom(proxyPo, domainPattern);
    }

    @Override
    public List<String> listTag() {
        List<String> ret = new ArrayList<>();
        QueryWrapper<ProxyPo> query = new QueryWrapper<>();
        query.isNull(ProxyPo.DELETE_FLAG);
        query.isNotNull(ProxyPo.TAG);
        query.ne(ProxyPo.TAG, StringPool.EMPTY);
        query.select(StrUtil.format("distinct {} as {}", ProxyPo.TAG, ProxyPo.TAG));
        List<Map<String, Object>> result = proxyDao.selectMaps(query);
        for (Map<String, Object> map : result) {
            String tag = (String) map.get(ProxyPo.TAG);
            ret.add(tag);
        }
        return ret;
    }

    private List<ProxyDto> fetchListByFilter(ProxyFilter filter) {
        String name = filter.getName();
        String version = filter.getVersion();
        String tags = filter.getTags();
        Long projectId = filter.getProjectId();

        QueryWrapper<ProxyPo> query = new QueryWrapper<>();
        query.isNull(ProxyPo.DELETE_FLAG);
        if(projectId != null) {
            query.eq(ProxyPo.PROJECT_ID, projectId);
        }
        if(StrUtil.isNotBlank(name)) {
            query.like(ProxyPo.NAME, name);
        }
        if(StrUtil.isNotBlank(version)) {
            query.eq(ProxyPo.VERSION, version);
        }
        List<ProxyPo> proxyPoList = proxyDao.selectList(query);

        return proxyPoList.stream().filter(po -> {
            if (StrUtil.isNotBlank(tags) && !StrUtil.contains(tags, po.getTag())) {
                return false;
            }
            return true;
        }).map(po -> ProxyDtoConverter.createFrom(po, domainPattern)).collect(Collectors.toList());

    }

    private boolean existByNameAndVersion(String name, String version) {
        QueryWrapper<ProxyPo> query = new QueryWrapper<>();
        query.isNull(ProxyPo.DELETE_FLAG);
        query.eq(ProxyPo.NAME, name);
        query.eq(ProxyPo.VERSION, version);

        return proxyDao.selectCount(query) > 0;
    }

    private boolean existByDomainIdentifier(String domainIdentifier) {
        // ??????????????????????????????????????????
        QueryWrapper<ProxyPo> query = new QueryWrapper<>();
        query.eq(ProxyPo.DOMAIN, domainIdentifier);
        return proxyDao.selectCount(query) > 0;
    }
}
