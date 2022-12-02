package com.brandnewdata.mop.poc.proxy.bo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProxyEndpointSceneBo {
    private Long envId;

    private String envName;

    private Long sceneId;

    private String sceneName;

    private Long versionId;

    private String versionName;

    private String processId;

    private String processName;
}
