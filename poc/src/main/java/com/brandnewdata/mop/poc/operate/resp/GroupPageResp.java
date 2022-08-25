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
     * 活动实例数
     */
    private int activeInstanceCount;

    /**
     * 异常实例数
     */
    private int incidentInstanceCount;

}
