package com.brandnewdata.mop.poc.proxy.req;

import lombok.Data;

@Data
public class ImportFromFileReq {

    /**
     * 文件类型：JSON，YAML
     */
    private String fileType;

    /**
     * 文件内容
     */
    private int fileContent;
}
