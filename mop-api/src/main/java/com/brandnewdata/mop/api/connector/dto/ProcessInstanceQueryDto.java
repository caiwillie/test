package com.brandnewdata.mop.api.connector.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProcessInstanceQueryDto {
    private int pageNum;

    private int pageSize;

    private List<String> modelKeyList;
}
