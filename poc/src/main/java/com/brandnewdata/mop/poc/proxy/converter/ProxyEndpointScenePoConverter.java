package com.brandnewdata.mop.poc.proxy.converter;

import com.brandnewdata.mop.poc.proxy.bo.ProxyEndpointSceneBo;
import com.brandnewdata.mop.poc.proxy.po.ProxyEndpointScenePo;
import com.dxy.library.json.jackson.JacksonUtil;

public class ProxyEndpointScenePoConverter {

    public static void updateFrom(ProxyEndpointScenePo target, ProxyEndpointSceneBo bo) {
        target.setEnvId(bo.getEnvId());
        target.setEnvName(bo.getEnvName());
        target.setSceneId(bo.getSceneId());
        target.setSceneName(bo.getSceneName());
        target.setVersionId(bo.getVersionId());
        target.setVersionName(bo.getVersionName());
        target.setProcessId(bo.getProcessId());
        target.setProcessName(bo.getProcessName());
    }
}
