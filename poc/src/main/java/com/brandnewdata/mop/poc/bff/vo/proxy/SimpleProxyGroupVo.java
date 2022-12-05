package com.brandnewdata.mop.poc.bff.vo.proxy;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SimpleProxyGroupVo {

    /**
     * api 名称
     */
    private String name;

    /**
     * 版本列表
     */
    private List<SimpleProxyVo> versionList;
}
