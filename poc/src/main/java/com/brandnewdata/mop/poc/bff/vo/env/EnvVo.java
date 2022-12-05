package com.brandnewdata.mop.poc.bff.vo.env;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EnvVo {

    /**
     * 环境id
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 部署时间
     */
    private LocalDateTime deployTime;

    /**
     * 环境名称
     */
    private String name;

    /**
     * 状态
     */
    private String status;

    /**
     * 描述
     */
    private String description;

    /**
     * CPU使用率
     */
    private Double cpuUsageRate;

    /**
     * 内存使用率
     */
    private Double memoryUsageRate;

    /**
     * 存储使用率
     */
    private Double storageUsageRate;

    /**
     * 部署场景数
     */
    private Integer sceneCount;

    /**
     * 部署流程数
     */
    private Integer processCount;


}
