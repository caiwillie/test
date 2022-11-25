package com.brandnewdata.mop.poc.process.bo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ZeebeDeployBo {

    private String processId;

    private Long zeebeKey;

    private String zeebeXml;

    private int zeebeVersion;

}
