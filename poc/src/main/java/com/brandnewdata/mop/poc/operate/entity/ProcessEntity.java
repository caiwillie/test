package com.brandnewdata.mop.poc.operate.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProcessEntity extends OperateZeebeEntity<ProcessEntity> {

    private String name;

    private int version;

    private String bpmnProcessId;

    private String bpmnXml;

    private String resourceName;

}
