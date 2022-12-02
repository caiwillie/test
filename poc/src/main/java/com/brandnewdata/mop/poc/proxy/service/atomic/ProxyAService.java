package com.brandnewdata.mop.poc.proxy.service.atomic;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.proxy.converter.ProxyDtoConverter;
import com.brandnewdata.mop.poc.proxy.dao.ProxyDao;
import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;
import com.brandnewdata.mop.poc.proxy.po.ProxyPo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProxyAService implements IProxyAService {
    @Resource
    private ProxyDao proxyDao;

    @Override
    public Map<Long, ProxyDto> fetchById(List<Long> idList) {
        if(CollUtil.isEmpty(idList)) return MapUtil.empty();
        Assert.isFalse(CollUtil.hasNull(idList), "idList can not contain null");

        QueryWrapper<ProxyPo> query = new QueryWrapper<>();
        query.in(ProxyPo.ID, idList);

        List<ProxyPo> proxyPoList = proxyDao.selectList(query);
        return proxyPoList.stream().map(ProxyDtoConverter::createFrom)
                .collect(Collectors.toMap(ProxyDto::getId, Function.identity()));
    }

    @Override
    public ProxyDto fetchByDomain(String domain) {
        return null;
    }
}
