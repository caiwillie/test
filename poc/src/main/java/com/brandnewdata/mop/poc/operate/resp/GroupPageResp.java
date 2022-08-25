package com.brandnewdata.mop.poc.operate.resp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupPageResp {

    /**
     * 流程 id
     */
    private String processId;

    /**
     * 流程名称
     */
    private String processName;

    /**
     * 版本数量
     */
    private int versionCount;

    /**
     * 成功实例数
     */
    private int successInstanceCount;

    /**
     * 失败实例数
     */
    private int failInstanceCount;

}
