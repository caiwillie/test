package com.brandnewdata.mop.poc.process.dto;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.brandnewdata.mop.poc.process.entity.ProcessDeployEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程部署实体
 */
@Data
public class ProcessDeployDto {

    /**
     * 部署 id
     */
    private Long id;

    /**
     * 部署时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 流程 id
     */
    private String processId;

    /**
     * 流程名称
     */
    private String processName;

    /**
     * 部署 版本
     */
    private int version;

    /**
     * 类型：1 场景，2 触发器，3 操作
     */
    private int type;

    /**
     * zeebe 触发 key
     */
    private Long zeebeKey;

    /**
     * 触发器id
     */
    private String trigger;

    /**
     * 部署 xml
     */
    private String xml;

    /**
     * 部署的 zeebe xml
     */
    private String zeebeXml;


    public ProcessDeployDto from(ProcessDeployEntity entity, boolean withXml) {
        if(entity == null) return this; //为空返回
        this.setId(entity.getId());
        this.setCreateTime(LocalDateTimeUtil.of(entity.getCreateTime()));
        this.setUpdateTime(LocalDateTimeUtil.of(entity.getUpdateTime()));
        this.setProcessId(entity.getProcessId());
        this.setProcessName(entity.getProcessName());
        this.setVersion(entity.getVersion());
        this.setType(entity.getType());
        this.setZeebeKey(entity.getZeebeKey());
        this.setTrigger(entity.getTrigger());
        if(withXml) {
            this.setXml(entity.getProcessXml());
            this.setZeebeXml(entity.getZeebeXml());
        }
        return this;
    }

}
