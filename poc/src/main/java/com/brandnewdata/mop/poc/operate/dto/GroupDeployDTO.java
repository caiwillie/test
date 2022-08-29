package com.brandnewdata.mop.poc.operate.dto;

import com.brandnewdata.mop.poc.process.dto.ProcessDeployDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class GroupDeployDTO {
    /**
     * 流程 id
     */
    private String processId;

    /**
     * 流程名称
     */
    private String processName;

    /**
     * 最后一次更新版本
     */
    private int latestVersion;

    /**
     * 最后一个更新时间
     */
    private LocalDateTime latestUpdateTime;

    /**
     * 部署版本列表
     */
    private List<ProcessDeployDTO> deploys = new ArrayList<>();

}
