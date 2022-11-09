package com.brandnewdata.mop.poc.operate.resp;

import com.brandnewdata.mop.poc.process.dto.ProcessDeployDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GroupDeployResp {

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


    /**
     * 部署的列表
     */
    private List<ProcessDeployDto> deploys;

}
