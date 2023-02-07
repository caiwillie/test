package com.brandnewdata.mop.poc.bff.vo.proxy;

import lombok.Data;

@Data
public class InspectVo {

    /**
     * 描述文件格式：YAML
     */
    private String format;

    /**
     * 描述文件内容
     */
    private String content;
}
