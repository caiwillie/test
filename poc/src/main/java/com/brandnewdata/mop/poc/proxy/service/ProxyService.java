package com.brandnewdata.mop.poc.proxy.service;

import cn.hutool.core.collection.CollUtil;
import com.brandnewdata.mop.poc.constant.ProxyConst;
import com.brandnewdata.mop.poc.proxy.dto.old.Endpoint;
import com.brandnewdata.mop.poc.proxy.dto.old.ImportDTO;
import com.brandnewdata.mop.poc.proxy.dto.old.Proxy;
import com.brandnewdata.mop.poc.proxy.req.ImportFromFileReq;
import com.brandnewdata.mop.poc.proxy.util.SwaggerUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author caiwillie
 */
@Service
public class ProxyService {

    @Resource
    private EndpointService endpointService;

    public void importFromFile(ImportFromFileReq req) {
        String content = req.getFileContent();
        ImportDTO dto = SwaggerUtil.parse(content);
        // 保存 proxy
        Proxy proxy = dto.getProxy();

        // 从文件导入的状态都设置为开发中
        proxy.setState(ProxyConst.PROXY_STATE__DEVELOPING);
        // proxy = save(proxy);
        Long proxyId = proxy.getId();

        // 保存endpoints
        List<Endpoint> endpointList = dto.getEndpointList();
        if(CollUtil.isNotEmpty(endpointList)) {
            for (Endpoint endpoint : endpointList) {
                endpoint.setProxyId(proxyId);
                endpointService.save(endpoint);
            }
        }
    }

}
