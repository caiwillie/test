package com.brandnewdata.mop.api.scene.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class VersionProcessStartDto {

    /**
     * 流程相关信息（json字符串）
     */
    private String processRelevantInfo;

    /**
     * 流程入参
     */
    private Map<String, Object> variables;
}
