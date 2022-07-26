package com.brandnewdata.mop.poc.process.dto;

import lombok.Data;

@Data
public class ProcessDeploy {

    private String processId;

    private String processName;

    private String xml;

    private int version;

    private int type;

}
