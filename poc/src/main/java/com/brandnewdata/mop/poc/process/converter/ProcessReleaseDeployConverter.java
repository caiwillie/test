package com.brandnewdata.mop.poc.process.converter;

import com.brandnewdata.mop.poc.process.bo.ZeebeDeployBo;
import com.brandnewdata.mop.poc.process.po.ProcessReleaseDeployPo;

public class ProcessReleaseDeployConverter {

    public static ProcessReleaseDeployPo createFrom(ZeebeDeployBo bo) {
        ProcessReleaseDeployPo po = new ProcessReleaseDeployPo();
        po.setProcessId(bo.getProcessId());
        po.setProcessZeebeKey(bo.getZeebeKey());
        po.setProcessZeebeXml(bo.getZeebeXml());
        po.setProcessZeebeVersion(bo.getZeebeVersion());
        return po;
    }

    public static void updateFrom(ZeebeDeployBo bo, ProcessReleaseDeployPo po) {
        po.setProcessZeebeKey(bo.getZeebeKey());
        po.setProcessZeebeVersion(bo.getZeebeVersion());
        po.setProcessZeebeXml(bo.getZeebeXml());
    }

}
