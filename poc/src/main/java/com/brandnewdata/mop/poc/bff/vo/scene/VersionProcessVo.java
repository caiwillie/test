package com.brandnewdata.mop.poc.bff.vo.scene;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
public class VersionProcessVo {

    private Long id;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long versionId;

    private String processId;

    private String processName;

    private String processXml;

    private String processImg;

    private Map<String, Object> variables;
}
