package com.brandnewdata.mop.poc.operate.po;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProcessPo extends OperateZeebePo<ProcessPo> {

    private String name;

    private int version;

    private String bpmnProcessId;

    private String bpmnXml;

    private String resourceName;

}
