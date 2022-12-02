package com.brandnewdata.mop.poc.proxy.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.proxy.converter.ProxyEndpointCallDtoConverter;
import com.brandnewdata.mop.poc.proxy.dao.ProxyEndpointCallDao;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointCallDto;
import com.brandnewdata.mop.poc.proxy.po.ProxyEndpointCallPo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProxyEndpointCallService implements IProxyEndpointCallService {

    @Resource
    private ProxyEndpointCallDao proxyEndpointCallDao;

    @Override
    public Page<ProxyEndpointCallDto> pageByEndpointId(Integer pageNum, Integer pageSize,
                                                       List<Long> endpointIdList) {
        Assert.isTrue(pageNum > 0, "pageNum must be greater than 0");
        Assert.isTrue(pageSize > 0, "pageSize must be greater than 0");
        if(CollUtil.isEmpty(endpointIdList)) return Page.empty();

        QueryWrapper<ProxyEndpointCallPo> query = new QueryWrapper<>();
        query.in(ProxyEndpointCallPo.ENDPOINT_ID, endpointIdList);
        query.orderByDesc(ProxyEndpointCallPo.CREATE_TIME);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ProxyEndpointCallPo> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        page = proxyEndpointCallDao.selectPage(page, query);

        List<ProxyEndpointCallPo> records = page.getRecords();
        List<ProxyEndpointCallDto> dtoList = records.stream()
                .map(ProxyEndpointCallDtoConverter::createFrom).collect(Collectors.toList());
        return new Page<>(page.getTotal(), dtoList);
    }

}
