package com.brandnewdata.mop.poc.bff.converter.process;

import com.brandnewdata.mop.poc.bff.vo.scene.DebugProcessInstanceVo;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDto;

public class ProcessInstanceVoConverter {


    /*
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
    * */
    public static DebugProcessInstanceVo createFrom(ListViewProcessInstanceDto dto) {
        DebugProcessInstanceVo vo = new DebugProcessInstanceVo();
        vo.setProcessId();
    }
}
