package com.brandnewdata.mop.poc.proxy.resp;

import lombok.Data;

import java.util.List;

@Data
public class ApiResp {

    /**
     * API 名称
     */
    private String name;

    /**
     * 版本列表
     */
    private List<VersionSpecifiedResp> versions;

}
