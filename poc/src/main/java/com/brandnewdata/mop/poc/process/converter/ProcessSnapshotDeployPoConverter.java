package com.brandnewdata.mop.poc.process.converter;

import com.brandnewdata.mop.poc.process.bo.ZeebeDeployBo;
import com.brandnewdata.mop.poc.process.po.ProcessSnapshotDeployPo;

public class ProcessSnapshotDeployPoConverter {

    public static ProcessSnapshotDeployPo createFrom(ZeebeDeployBo bo) {
        ProcessSnapshotDeployPo po = new ProcessSnapshotDeployPo();
        po.setProcessId(bo.getProcessId());
        po.setProcessZeebeKey(bo.getZeebeKey());
        po.setProcessZeebeVersion(bo.getZeebeVersion());
        po.setProcessZeebeXml(bo.getZeebeXml());
        return po;
    }

    public static void updateFrom(ProcessSnapshotDeployPo po, Long envId, String digest, String processXml) {
        po.setEnvId(envId);
        po.setProcessDigest(digest);
        po.setProcessXml(processXml);
    }
}
