package com.brandnewdata.mop.poc.process.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author caiwillie
 */
@Data
public class ProcessDefinition {

    private String processId;

    private String name;

    private String xml;

    private String imgUrl;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
