package com.brandnewdata.mop.poc.process.converter;

import cn.hutool.core.util.IdUtil;
import com.brandnewdata.mop.poc.constant.ProcessConst;
import com.brandnewdata.mop.poc.process.po.ProcessDeployTaskPo;

public class ProcessDeployTaskPoConverter {

    public static ProcessDeployTaskPo createFrom(String processId, String processName,
                                          String processXml, String zeebeXml) {
        ProcessDeployTaskPo po = new ProcessDeployTaskPo();
        po.setId(IdUtil.getSnowflakeNextId());
        po.setProcessId(processId);
        po.setProcessName(processName);
        po.setProcessXml(processXml);
        po.setProcessZeebeXml(zeebeXml);
        po.setDeployStatus(ProcessConst.PROCESS_DEPLOY_STATUS__UNDEPLOY);
        return po;
    }
}
