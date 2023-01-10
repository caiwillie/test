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
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.constant.ProxyConst;
import com.brandnewdata.mop.poc.proxy.bo.ProxyFilter;
import com.brandnewdata.mop.poc.proxy.cache.ProxyCache;
import com.brandnewdata.mop.poc.proxy.converter.ProxyDtoConverter;
import com.brandnewdata.mop.poc.proxy.converter.ProxyPoConverter;
import com.brandnewdata.mop.poc.proxy.dao.ProxyDao;
import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyGroupDto;
import com.brandnewdata.mop.poc.proxy.po.ProxyPo;
import com.brandnewdata.mop.poc.util.CollectorsUtil;
import com.brandnewdata.mop.poc.util.PageEnhancedUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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

        // 组装结果
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

        // filter
        return proxyCache.asMap().values().stream().filter(proxyDto -> {
            if (StrUtil.isBlank(name)) return true;
            if (!StrUtil.equals(proxyDto.getName(), name)) return false;
            if (StrUtil.isBlank(version)) return true;
            if (!StrUtil.equals(proxyDto.getVersion(), version)) return false;
            if (StrUtil.isBlank(tags)) return true;
            return StrUtil.contains(tags, proxyDto.getTag());
        }).collect(Collectors.toList());
    }

    @Override
    public ProxyDto save(ProxyDto proxyDto, boolean imported) {
        String name = proxyDto.getName();
        String version = proxyDto.getVersion();
        Assert.notNull(proxyDto.getProjectId(), "project id can not be null");
        Assert.notNull(name, "API名称不能为空");
        Assert.notNull(version, "API版本不能为空");

        Long id = proxyDto.getId();
        if(id == null) {
            // 名称和版本唯一
            Assert.isFalse(existByNameAndVersion(name, version), "{} {} 已存在", name, version);

            // 生成唯一的域名标识
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
            Assert.notNull(oldProxyDto, "API不存在");
            Assert.isTrue(StrUtil.equals(oldProxyDto.getName(), name), "API名称不能修改");
            Assert.isTrue(StrUtil.equals(oldProxyDto.getVersion(), version), "API版本不能修改");

            ProxyPoConverter.updateFrom(oldProxyDto, proxyDto);
            // 注意需要将domain设置成null
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
        // 解析domain，获取正则表达式中的第一个括号中的内容
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
                "状态不能为空，且只能是 停止或运行");
        proxyPo.setState(proxyDto.getState());
        proxyDao.updateById(proxyPo);
        return ProxyDtoConverter.createFrom(proxyPo, domainPattern);
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
            if (tags != null && !StrUtil.contains(tags, po.getTag())) {
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
        // 全局唯一（包括已删除的接口）
        QueryWrapper<ProxyPo> query = new QueryWrapper<>();
        query.eq(ProxyPo.DOMAIN, domainIdentifier);
        return proxyDao.selectCount(query) > 0;
    }
}
