package com.brandnewdata.mop.poc.bff.bo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HomeApiStatisticCountBo {

    /**
     * API 服务数
     */
    int apiCount;

    /**
     * API 路径数
     */
    int apiPathCount;

    public HomeApiStatisticCountBo(int apiCount, int apiPathCount) {
        this.apiCount = apiCount;
        this.apiPathCount = apiPathCount;
    }
}
