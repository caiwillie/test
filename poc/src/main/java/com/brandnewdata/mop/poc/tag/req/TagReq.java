package com.brandnewdata.mop.poc.tag.req;

import lombok.Data;

@Data
public class TagReq {

    /**
     * 标签名字
     */
    private String name;

    /**
     * 业务类型
     */
    private String businessType;
}
