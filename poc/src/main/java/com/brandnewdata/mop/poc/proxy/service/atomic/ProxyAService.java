package com.brandnewdata.mop.poc.proxy.service.atomic;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.constant.ProxyConst;
import com.brandnewdata.mop.poc.proxy.converter.ProxyDtoConverter;
import com.brandnewdata.mop.poc.proxy.converter.ProxyPoConverter;
import com.brandnewdata.mop.poc.proxy.dao.ProxyDao;
import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyGroupDto;
import com.brandnewdata.mop.poc.proxy.po.ProxyPo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProxyAService implements IProxyAService {
    @Resource
    private ProxyDao proxyDao;


    private final String domainRegEx;


    private final String domainPattern;

    public ProxyAService(@Value("${brandnewdata.api.domainRegEx}") String domainRegEx,
                         @Value("${brandnewdata.api.domainPattern}") String domainPattern) {
        this.domainRegEx = domainRegEx;
        this.domainPattern = domainPattern;
    }

    @Override
    public Page<ProxyGroupDto> pageGroupByName(Integer pageNum, Integer pageSize,
                                               String name, String tags) {
        Assert.notNull(pageNum > 0, "pageNum must be greater than 0");
        Assert.notNull(pageSize > 0, "pageSize must be greater than 0");

        // 根据 name 分组
        QueryWrapper<ProxyPo> query = new QueryWrapper<>();
        if (StrUtil.isNotBlank(name)) {
            query.like(ProxyPo.NAME, name);
        }
        query.orderByDesc(ProxyPo.UPDATE_TIME);
        query.groupBy(ProxyPo.NAME);
        query.select(ProxyPo.NAME, "count(*) as count");

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Map<String, Object>> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        page = proxyDao.selectMapsPage(page, query);

        List<Map<String, Object>> records = page.getRecords();
        if(CollUtil.isEmpty(records)) return Page.empty();

        List<String> nameList = records.stream().map(map -> (String) map.get(ProxyPo.NAME)).collect(Collectors.toList());

        // 根据 name 查询 proxy
        QueryWrapper<ProxyPo> query2 = new QueryWrapper<>();
        query2.in(ProxyPo.NAME, nameList);
        List<ProxyPo> proxyPoList = proxyDao.selectList(query2);

        Map<String, List<ProxyDto>> proxyListMap = proxyPoList.stream().map(po -> ProxyDtoConverter.createFrom(po, domainPattern))
                .collect(Collectors.groupingBy(ProxyDto::getName));

        // 组装结果
        List<ProxyGroupDto> ret = new ArrayList<>();
        for (String _name : nameList) {
            ProxyGroupDto proxyGroupDto = new ProxyGroupDto();
            proxyGroupDto.setName(_name);
            proxyGroupDto.setProxyDtoList(proxyListMap.get(_name));
            ret.add(proxyGroupDto);
        }

        return new Page<>(page.getTotal(), ret);
    }

    @Override
    public ProxyDto save(ProxyDto proxyDto, boolean imported) {
        String name = proxyDto.getName();
        String version = proxyDto.getVersion();
        Assert.notNull(name, "API名称不能为空");
        Assert.notNull(version, "API版本不能为空");

        Long id = proxyDto.getId();
        if(id == null) {
            // 名称和版本唯一
            Assert.isFalse(existByNameAndVersion(name, version), "API名称和版本不能重复");

            String domain = DigestUtil.md5Hex(StrUtil.format("{}:{}", name, version));
            proxyDto.setDomain(domain);

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

            ProxyPoConverter.updateFrom(oldProxyDto, oldProxyDto);
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
        QueryWrapper<ProxyPo> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq(ProxyPo.DOMAIN, identity);
        ProxyPo proxyPo = proxyDao.selectOne(queryWrapper1);
        return ProxyDtoConverter.createFrom(proxyPo, domainRegEx);
    }


    private boolean existByNameAndVersion(String name, String version) {
        QueryWrapper<ProxyPo> query = new QueryWrapper<>();
        query.eq(ProxyPo.NAME, name);
        query.eq(ProxyPo.VERSION, version);

        return proxyDao.selectCount(query) > 0;
    }
}
