package com.brandnewdata.mop.poc.bff.vo.proxy;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImportVo {

    /**
     * 项目id
     */
    private String projectId;

    /**
     * 文件类型：JSON, YAML
     */
    private String fileType;

    /**
     * 文件内容
     */
    private String fileContent;
}
