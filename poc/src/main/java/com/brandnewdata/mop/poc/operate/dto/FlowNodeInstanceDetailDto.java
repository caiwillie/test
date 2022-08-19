package com.brandnewdata.mop.poc.operate.dto;

import lombok.Data;

@Data
public class FlowNodeInstanceDetailDto {

    /**
     * 基本信息
     */
    private FlowNodeInstanceMetaDataDto meteData;


    /**
     * 异常信息
     */
    private IncidentDto incident;

}
