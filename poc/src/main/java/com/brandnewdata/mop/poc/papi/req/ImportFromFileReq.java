package com.brandnewdata.mop.poc.papi.req;

import lombok.Data;

@Data
public class ImportFromFileReq {

    /**
     * 文件类型：YAML
     */
    private String fileType;

    /**
     * 文件内容
     */
    private String fileContent;
}
