package com.brandnewdata.mop.poc.bff.converter.process;

import com.brandnewdata.mop.poc.bff.vo.process.ProcessDefinitionVo;

public class ProcessDefinitionVoConverter {

    public static ProcessDefinitionVo createFrom(String processId, String processName, String processXml) {
        ProcessDefinitionVo vo = new ProcessDefinitionVo();
        vo.setProcessId(processId);
        vo.setProcessName(processName);
        vo.setProcessXml(processXml);
        return vo;
    }
}
