package com.brandnewdata.mop.poc.proxy.resp;

import lombok.Data;

@Data
public class InspectResp {

    /**
     * 描述文件格式：YAML
     */
    private String format;

    /**
     * 描述文件内容
     */
    private String content;
}
