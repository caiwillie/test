package com.brandnewdata.mop.poc.operate.dto;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.brandnewdata.mop.poc.operate.po.FlowNodeInstancePo;
import com.brandnewdata.mop.poc.operate.po.FlowNodeType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
public class FlowNodeInstanceTreeNodeDto extends OperateZeebeDto implements FromOneEntity<FlowNodeInstanceTreeNodeDto, FlowNodeInstancePo> {


    /**
     * 节点id
     */
    private String flowNodeId;

    /**
     * 开始时间
     */
    private LocalDateTime startDate;

    /**
     * 结束时间
     */
    private LocalDateTime endDate;

    /**
     * 节点状态
     * ACTIVE 运行中, INCIDENT 异常, COMPLETED 完成, TERMINATED 取消,
     */
    private FlowNodeStateDto state;

    /**
     * 节点类型
     * PROCESS,
     * SUB_PROCESS,
     * EVENT_SUB_PROCESS,
     * START_EVENT,
     * INTERMEDIATE_CATCH_EVENT,
     * INTERMEDIATE_THROW_EVENT,
     * BOUNDARY_EVENT,
     * END_EVENT,
     * SERVICE_TASK,
     * RECEIVE_TASK,
     * USER_TASK,
     * MANUAL_TASK,
     * EXCLUSIVE_GATEWAY,
     * PARALLEL_GATEWAY,
     * EVENT_BASED_GATEWAY,
     * SEQUENCE_FLOW,
     * MULTI_INSTANCE_BODY,
     * CALL_ACTIVITY,
     * BUSINESS_RULE_TASK,
     * SCRIPT_TASK,
     * SEND_TASK,
     */
    private FlowNodeType type;


    /**
     * 异常id
     */
    private Long incidentKey;

    /**
     * 流程实例id
     */
    private Long processInstanceKey;

    private String treePath;

    private int level;

    private Long position;

    private boolean incident;

    private List<FlowNodeInstanceTreeNodeDto> children;

    @Override
    public FlowNodeInstanceTreeNodeDto from(FlowNodeInstancePo entity) {
        this.setId(entity.getId());
        this.setFlowNodeId(entity.getFlowNodeId());
        this.setStartDate(Optional.ofNullable(entity.getStartDate())
                .map(offsetDateTime -> LocalDateTimeUtil.of(offsetDateTime.toInstant())).orElse(null));
        this.setEndDate(Optional.ofNullable(entity.getEndDate())
                .map(offsetDateTime -> LocalDateTimeUtil.of(offsetDateTime.toInstant())).orElse(null));
        this.setState(FlowNodeStateDto.getState(entity.getState()));
        this.setType(entity.getType());
        this.setIncidentKey(entity.getIncidentKey());
        this.setProcessInstanceKey(entity.getProcessInstanceKey());
        this.setTreePath(entity.getTreePath());
        this.setLevel(entity.getLevel());
        this.setPosition(entity.getPosition());
        this.setIncident(entity.isIncident());
        return this;
    }


}
