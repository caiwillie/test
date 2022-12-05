package com.brandnewdata.mop.poc.bff.vo.proxy;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProxyGroupVo {
    /**
     * proxy name
     */
    private String name;

    /**
     * version list
     */
    private List<ProxyVo> versions;
}
