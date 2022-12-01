package com.brandnewdata.mop.poc.papi.service;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import com.brandnewdata.mop.poc.papi.dao.ReverseProxyDao;
import com.brandnewdata.mop.poc.papi.entity.ReverseProxyEntity;
import com.brandnewdata.mop.poc.papi.req.ImportFromFileReq;
import com.dxy.library.json.jackson.JacksonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

class ProxyServiceTest {
    @Mock
    ReverseProxyDao proxyDao;
    @Mock
    EndpointService endpointService;

    @InjectMocks
    ProxyService proxyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testPageV2() {
        when(proxyDao.selectList(any())).thenReturn(testPageV2Data1());
        when(endpointService.listByProxyIdList(any())).thenReturn(ListUtil.empty());

        // call method
        proxyService.pageV2(1, 10, null, null);
    }

    private List<ReverseProxyEntity> testPageV2Data1() {
        ReverseProxyEntity entity1 = new ReverseProxyEntity();
        entity1.setUpdateTime(DateUtil.parseDateTime("2022-09-20 00:00:00"));
        entity1.setName("a");
        entity1.setVersion("1.0.0");

        ReverseProxyEntity entity2 = new ReverseProxyEntity();
        entity2.setUpdateTime(DateUtil.parseDateTime("2022-09-20 01:00:00"));
        entity2.setName("a");
        entity2.setVersion("2.0.0");

        ReverseProxyEntity entity3 = new ReverseProxyEntity();
        entity3.setUpdateTime(DateUtil.parseDateTime("2022-09-21 00:00:00"));
        entity3.setName("b");
        entity3.setVersion("1.0.0");

        ReverseProxyEntity entity4 = new ReverseProxyEntity();
        entity4.setUpdateTime(DateUtil.parseDateTime("2022-09-21 01:00:00"));
        entity4.setName("b");
        entity4.setVersion("2.0.0");

        return ListUtil.of(entity1, entity2, entity3, entity4);
    }

    @Test
    void importFromFile() {
        String source = "{\"fileContent\":\"{\\n  \\\"openapi\\\" : \\\"3.0.1\\\",\\n  \\\"info\\\" : {\\n    \\\"title\\\" : \\\"前端text\\\",\\n    \\\"version\\\" : \\\"1.0.0\\\"\\n  },\\n  \\\"paths\\\" : {\\n    \\\"/test\\\" : { },\\n    \\\"/test2\\\" : { },\\n    \\\"/test111\\\" : { },\\n    \\\"/test11\\\" : { }\\n  }\\n}\",\"fileType\":\"JSON\"}";
        ImportFromFileReq req = JacksonUtil.from(source, ImportFromFileReq.class);
        when(endpointService.save(any())).thenReturn(null);
        proxyService.importFromFile(req);
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme