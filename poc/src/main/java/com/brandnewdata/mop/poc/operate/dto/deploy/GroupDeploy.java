package com.brandnewdata.mop.poc.operate.dto.deploy;

import lombok.Data;

@Data
public class GroupDeploy {
    /**
     * 流程 id
     */
    private String processId;

    /**
     * 流程名称
     */
    private String processName;

    /**
     * 版本数
     */
    private int versionCount;

    /**
     * 通过实例数
     */
    private int successCount;

    /**
     * 异常实例数
     */
    private int failCount;
}
