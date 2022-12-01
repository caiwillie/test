package com.brandnewdata.mop.poc.papi.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.papi.dao.ReverseProxyDao;
import com.brandnewdata.mop.poc.papi.dao.ReverseProxyEndpointDao;
import com.brandnewdata.mop.poc.papi.dto.Backend;
import com.brandnewdata.mop.poc.papi.dto.ForwardConfig;
import com.brandnewdata.mop.poc.papi.dto.ProcessConfig;
import com.brandnewdata.mop.poc.papi.po.ReverseProxyEndpointPo;
import com.brandnewdata.mop.poc.papi.po.ReverseProxyPo;
import com.dxy.library.json.jackson.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class BackendService {

    @Resource
    private ReverseProxyDao proxyDao;

    @Resource
    private ReverseProxyEndpointDao proxyEndpointDao;

    @Value("${brandnewdata.api.domainRegEx}")
    private String domainRegEx;

    public Backend getBackend(String domain, String uri) {
        // 解析domain，获取正则表达式中的第一个括号中的内容
        domain = ReUtil.getGroup1(domainRegEx, domain);

        QueryWrapper<ReverseProxyPo> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq(ReverseProxyPo.DOMAIN, domain);
        ReverseProxyPo proxy = proxyDao.selectOne(queryWrapper1);

        // 没找到就直接返回 null
        if(proxy == null) {
            return null;
        }

        Long proxyId = proxy.getId();
        QueryWrapper<ReverseProxyEndpointPo> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.eq(ReverseProxyEndpointPo.PROXY_ID, proxyId);
        queryWrapper2.eq(ReverseProxyEndpointPo.LOCATION, uri);

        List<ReverseProxyEndpointPo> endpointList = proxyEndpointDao.selectList(queryWrapper2);

        // endpoint list 为空就直接返回
        if(CollUtil.isEmpty(endpointList)) return null;

        //取第一个
        ReverseProxyEndpointPo endpoint = endpointList.get(0);

        Integer backendType = endpoint.getBackendType();
        String backendConfig = endpoint.getBackendConfig();

        Object _backendConfig = convertBackendConfig(backendType, backendConfig);

        if(backendType != null && _backendConfig != null) {
            return new Backend(backendType, _backendConfig);
        } else {
            // 配置解析错误直接返回 null
            return null;
        }
    }



    private Object convertBackendConfig (Integer backendType, String backendConfig) {
        Object result = null;
        if(backendType == 1) {
            result = JacksonUtil.from(backendConfig, ProcessConfig.class);
        } else if (backendType == 2) {
            result = JacksonUtil.from(backendConfig, ForwardConfig.class);
        }
        return result;
    }

}
