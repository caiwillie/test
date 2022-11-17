package com.brandnewdata.mop.poc.operate.resp;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDto;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class ProcessInstanceResp {
    /**
     * 流程 id
     */
    private String processId;

    /**
     * 流程版本
     */
    private Integer version;

    /**
     * 流程实例 id
     */
    private String instanceId;

    /**
     * 父级流程实例 id
     */
    private String parentInstanceId;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 运行状态：ACTIVE 运行，INCIDENT 异常，COMPLETED 完成，CANCELED 取消
     */
    private String state;


    public ProcessInstanceResp from(ListViewProcessInstanceDto dto) {
        this.setProcessId(dto.getBpmnProcessId());
        this.setVersion(dto.getProcessVersion());
        this.setInstanceId(String.valueOf(dto.getId()));
        this.setParentInstanceId(Optional.ofNullable(dto.getParentInstanceId()).map(String::valueOf).orElse(null));
        this.setStartTime(Optional.ofNullable(dto.getStartDate()).map(LocalDateTimeUtil::formatNormal).orElse(null));
        this.setEndTime(Optional.ofNullable(dto.getEndDate()).map(LocalDateTimeUtil::formatNormal).orElse(null));
        this.setState(dto.getState().name());
        return this;
    }
}
